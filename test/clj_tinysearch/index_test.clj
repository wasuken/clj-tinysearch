(ns clj-tinysearch.index-test
  (:require [clojure.test :refer :all]
            [clj-tinysearch.index :refer :all]))

(deftest postings-list-add-test
  (testing "just like test name"
    (let [p (new-posting 1 1 2 3)
          pl (new-postings-list p)]
      (is (= (list-last pl) p))
      (is (= (:list (add pl p))
             (seq [p p])))
      (is (not (= (add-if-eq-doc-id (new-postings-list) p)
                  p))))))
