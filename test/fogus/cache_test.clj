(ns fogus.cache-test
  (:require [clojure.test :refer :all]
            [fogus.cache :as c]
            [fogus.cache.caffeine :as caff]
            [fogus.cache.guava :as guava]
            [fogus.cache.cache2k :as cache2k]
            [fogus.cache.eh :as eh]))

(set! *warn-on-reflection* true)

(deftest caffeine-test
  (let [cache (caff/build-cache)
        key1 (c/put cache "One")
        key2 (c/put cache "Two")
        key3 (c/put cache "One")]
    (is (= "One" (c/get cache key1)))
    (is (= "Two" (c/get cache key2)))
    (is (= "One" (c/get cache key3)))
    (is (= key1 key3))))

(deftest guava-test
  (let [cache (guava/build-cache)
        key1 (c/put cache "One")
        key2 (c/put cache "Two")
        key3 (c/put cache "One")]
    (is (= "One" (c/get cache key1)))
    (is (= "Two" (c/get cache key2)))
    (is (= "One" (c/get cache key3)))
    (is (= key1 key3))))

(deftest cache2k-test
  (let [cache (cache2k/build-cache)
        key1 (c/put cache "One")
        key2 (c/put cache "Two")
        key3 (c/put cache "One")]
    (is (= "One" (c/get cache key1)))
    (is (= "Two" (c/get cache key2)))
    (is (= "One" (c/get cache key3)))
    (is (= key1 key3))))

(deftest eh-test
  (let [cache (eh/build-cache)
        key1 (c/put cache "One")
        key2 (c/put cache "Two")
        key3 (c/put cache "One")]
    (is (= "One" (c/get cache key1)))
    (is (= "Two" (c/get cache key2)))
    (is (= "One" (c/get cache key3)))
    (is (= key1 key3))))
