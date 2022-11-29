(ns fogus.cache.guava
  (:require fogus.cache)
  (:import [com.google.common.cache Cache CacheBuilder RemovalListener RemovalNotification]
           [java.util UUID]
           [java.util.concurrent ConcurrentMap ConcurrentHashMap]))

(set! *warn-on-reflection* true)

(deftype GuavaCache [^ConcurrentMap identity-store
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
    (^void onRemoval [this ^RemovalNotification notification]
      (let [v (.getValue notification)]
        (f v)))))

(defn build-cache [& {:as opts}]
  (let [cache-builder (doto (CacheBuilder/newBuilder)
                        (.softValues))
        identity-store (ConcurrentHashMap.)
        object-store (.. cache-builder
                         (removalListener
                          (-> (fn [obj]
                                (.remove identity-store (System/identityHashCode obj)))
                              gc-removed-value-listener))
                         build)]
    (GuavaCache. identity-store object-store)))
