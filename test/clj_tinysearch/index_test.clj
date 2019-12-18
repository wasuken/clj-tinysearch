(ns clj-tinysearch.index-test
  (:require [clojure.test :refer :all]
            [clj-tinysearch.index :refer :all]))

(defn isnt [a b]
  (if (= a b)
    (is false)
    (is true)))

(deftest postings-list-add-test
  (testing "just like test name"
    (let [p (new-posting 1 [1 2 3])
          pl (new-postings-list p)]
      (is (list-last pl) p)
      (is (add pl p) (seq [p p]))
      (is (add-if-eq-doc-id pl p)
          (new-postings-list p p))
      (println (add-if-eq-doc-id (new-postings-list) p))
      (isnt (add-if-eq-doc-id (new-postings-list) p)
            p))))
