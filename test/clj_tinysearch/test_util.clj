(ns clj-tinysearch.test-util
  (:require [clojure.test :refer :all]))

(defn isnt [a b]
  (if (= a b)
    (is "a" "b")
    (is "" "")))

(defn assert-eqs [expected & vals]
  (if (every? #(= % expected) vals)
    (is "" "")
    (is "a" "b")))

(defn assert-eq [expected actual]
  (if (= expected actual)
    (is "" "")
    (is "a" "b")))
