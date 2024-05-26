package com.example.helloworld.service;

import io.github.bucket4j.*;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.local.LocalBucketSerializationHelper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.function.Supplier;

@Service
public class RateLimiterService {

  public static final String RATE_LIMITER = "rate-limiter";
  private final CacheManager cacheManager;
  private final Supplier<Bucket> bucketSupplier;

  public RateLimiterService(CacheManager cacheManager, Supplier<Bucket> bucketSupplier) {
    this.cacheManager = cacheManager;
    this.bucketSupplier = bucketSupplier;
  }

  public boolean isRateLimited(String key) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    // @TODO: Add synchronization or locking mechanism to avoid race conditions

    byte[] bucket = cacheManager.getCache(RATE_LIMITER).get(key, byte[].class); // key to Bucket
    Cache cache = cacheManager.getCache(RATE_LIMITER);
    if (bucket != null){
      // use reflection to access the private method fromBinarySnapshot
      Method method = LocalBucketSerializationHelper.class.getDeclaredMethod("fromBinarySnapshot", byte[].class);
      method.setAccessible(true); // Make the method accessible
      LocalBucket localBucket = (LocalBucket) method.invoke(null, bucket);
      var result = !localBucket.tryConsume(1);
      // update the cache with the updated bucket..i.e the current snapshot of the bucket
      cache.put(key, localBucket.toBinarySnapshot());
      return result;
    }

      LocalBucket localBucket = (LocalBucket) bucketSupplier.get();
      cache.put(key, localBucket.toBinarySnapshot());

    return false;
  }

  public Bucket getOrCreateBucket(String key, int limit, int seconds) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    // @TODO: Add synchronization or locking mechanism to avoid race conditions
    byte[] bucket = cacheManager.getCache(RATE_LIMITER).get(key, byte[].class);
    Cache cache = cacheManager.getCache(RATE_LIMITER);
    if (bucket != null){
      Method method = LocalBucketSerializationHelper.class.getDeclaredMethod("fromBinarySnapshot", byte[].class);
      method.setAccessible(true);
      LocalBucket localBucket = (LocalBucket) method.invoke(null, bucket);
      return localBucket;
    }

    Refill refill = Refill.greedy(seconds, Duration.ofSeconds(1));
    Bandwidth limit1 = Bandwidth.classic(limit, refill);
    LocalBucket bucket1 = Bucket.builder().addLimit(limit1).build();
    // put the bucket in the cache
    cache.put(key, bucket1.toBinarySnapshot());
    return bucket1;
  }

  // each time when the token is consumed, the current bucket is updated with the new snapshot
  public void updateBucket(String key, LocalBucket bucket) throws IOException {
    // @TODO: Add synchronization or locking mechanism
    cacheManager.getCache(RATE_LIMITER).put(key, bucket.toBinarySnapshot());
  }

  // reset the bucket
    public void removeKeyFromCache(String key) {
        cacheManager.getCache(RATE_LIMITER).evict(key);
    }

}