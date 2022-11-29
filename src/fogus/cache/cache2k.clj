(ns fogus.cache.cache2k
  (:require fogus.cache)
  (:import [org.cache2k Cache Cache2kBuilder CacheEntry]
           [org.cache2k.event CacheEntryOperationListener CacheEntryRemovedListener]
           [java.util UUID]
           [java.util.concurrent ConcurrentMap ConcurrentHashMap]))

(set! *warn-on-reflection* true)

(deftype Cache2kCache [^ConcurrentMap identity-store
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

(defn- gc-removed-value-listener
  "Return a cache removal listener that calls f on the removed value."
  ^CacheEntryOperationListener [f]
  (reify CacheEntryRemovedListener
    (^void onEntryRemoved [this ^Cache cache, ^CacheEntry entry]
     (f (.getValue entry)))))

(defn build-cache [& {:as opts}]
  (let [cache-builder (doto (Cache2kBuilder/forUnknownTypes)
                        (.entryCapacity 1024))
        identity-store (ConcurrentHashMap.)
        object-store (.. cache-builder
                         (addListener
                          (-> (fn [obj]
                                (.remove identity-store (System/identityHashCode obj)))
                              gc-removed-value-listener))
                         build)]
    (Cache2kCache. identity-store object-store)))
