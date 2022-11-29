(ns fogus.cache.caffeine
  (:require fogus.cache)
  (:import com.github.benmanes.caffeine.cache.Cache
           com.github.benmanes.caffeine.cache.Caffeine
           com.github.benmanes.caffeine.cache.RemovalListener
           java.util.UUID
           [java.util.concurrent ConcurrentMap ConcurrentHashMap]))

(set! *warn-on-reflection* true)

(deftype CaffeineCache [^ConcurrentMap identity-store
                     ^Cache object-store]
  fogus.cache/ReplicantCache
  (get [_ key]
    (.getIfPresent object-store key))

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
  ^RemovalListener [f]
  (reify RemovalListener
    (onRemoval [_this _k v _cause] (f v))))

(defn build-cache [& {:as opts}]
  (let [cache-builder (doto (com.github.benmanes.caffeine.cache.Caffeine/newBuilder)
                        (.softValues))
        identity-store (ConcurrentHashMap.)
        object-store (.. cache-builder
                         (removalListener
                          (-> (fn [obj]
                                (.remove identity-store (System/identityHashCode obj)))
                              gc-removed-value-listener))
                         build)]
    (CaffeineCache. identity-store object-store)))
