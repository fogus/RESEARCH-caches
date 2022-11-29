(ns fogus.cache.eh
  (:require fogus.cache)
  (:import org.ehcache.Cache
           org.ehcache.CacheManager
           org.ehcache.config.builders.CacheConfigurationBuilder
           org.ehcache.config.builders.CacheManagerBuilder
           org.ehcache.config.builders.ResourcePoolsBuilder
           org.ehcache.config.builders.CacheEventListenerConfigurationBuilder
           [java.util UUID]
           [java.util.concurrent ConcurrentMap ConcurrentHashMap]))

(set! *warn-on-reflection* true)

(deftype EHCache [^ConcurrentMap identity-store
                  ^Cache object-store]
  fogus.cache/ReplicantCache
  (get [_ key]
    (.get object-store key))

  (put [_ obj]
    (.computeIfAbsent
     identity-store
     (System/identityHashCode obj)
     (-> (fn [_] (let [rid (UUID/randomUUID)]
                   (.put object-store rid obj)
                   rid))
         fogus.cache/ju-function))))

#_(defn- gc-removed-value-listener
  "Return a cache removal listener that calls f on the removed value."
  ^RemovalListener [f]
  (reify RemovalListener
    (^void onRemoval [this ^RemovalNotification notification]
      (let [v (.getValue notification)]
        (f v)))))

(defn build-cache [& {:as opts}]
  (let [manager-builder (CacheManagerBuilder/newCacheManagerBuilder)
        config-builder (CacheConfigurationBuilder/newCacheConfigurationBuilder
                        java.lang.Object java.lang.Object
                        (ResourcePoolsBuilder/heap 10))
        identity-store (ConcurrentHashMap.)
        ^CacheManager cache-manager (.. manager-builder
                          (withCache "testEH" config-builder)
                          build)
;;        ^CacheEventListenerConfiguration cache-listener 
        _ (.init cache-manager)
        object-store (.getCache cache-manager "testEH" java.lang.Object java.lang.Object)]

    (EHCache. identity-store object-store)))

(comment
  (removalListener
   (-> (fn [obj]
         (.remove identity-store (System/identityHashCode obj)))
       gc-removed-value-listener))
)
