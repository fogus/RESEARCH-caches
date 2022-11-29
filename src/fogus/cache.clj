(ns fogus.cache
  (:refer-clojure :exclude [get put])
  (:import com.github.benmanes.caffeine.cache.Cache
           com.github.benmanes.caffeine.cache.Caffeine
           java.util.function.Function))

(defprotocol ReplicantCache
  (get [this key])
  (put [this obj]))

(defn ju-function
  "Wrap Clojure function as j.u.Function."
  ^Function [f]
  (reify Function
    (apply [_ x] (f x))))
