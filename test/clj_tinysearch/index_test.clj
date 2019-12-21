(ns clj-tinysearch.index-test
  (:require [clojure.test :refer :all]
            [clj-tinysearch.test-util :refer :all]
            [clj-tinysearch.index :refer :all]))

(deftest postings-list-add-test
  (testing "just like test name"
    (let [p (new-posting 1 1 2 3)
          pl (new-postings-list p)]
      (assert-eq (list-last pl) p)
      (assert-eq (add pl p) (seq [p p]))
      (assert-eq (add-if-eq-doc-id pl p)
          (new-postings-list p p))
      (isnt (add-if-eq-doc-id (new-postings-list) p)
            p))))
