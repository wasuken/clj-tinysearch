(ns clj-tinysearch.indexer-test
  (:require [clojure.test :refer :all]
            [clj-tinysearch.indexer :refer :all]
            [clj-tinysearch.util :refer :all]
            [clj-tinysearch.tokenizer :refer :all]
            [clj-tinysearch.index :refer :all])
  (:import [java.io ByteArrayInputStream]))

;;; will go to util

(deftest update-test
  (testing "update index test"
    (let [collections ["Do you quarrel sir?" "Quarrel sir! no sir!" "No better." "Well sir"]
          indexer (new-indexer (->Tokenizer))
          expected-index (->Index {"better"  (new-postings-list (new-posting 2 1)),
                                   "do"  (new-postings-list (new-posting 0 0)),
                                   "no"  (new-postings-list (new-posting 1 2) (new-posting 2 0)),
                                   "quarrel"  (new-postings-list (new-posting 0 2) (new-posting 1 0)),
                                   "sir"  (new-postings-list (new-posting 0 3) (new-posting 1 1 3) (new-posting 3 1)),
                                   "well"  (new-postings-list (new-posting 3 0)),
                                   "you"  (new-postings-list (new-posting 0 1))
                                   }
                                  4)
          actual-indexer (reduce-indexed (fn [idxr i x]
                                         (idxr-update idxr
                                                      i
                                                      (ByteArrayInputStream. (.getBytes x))))
                                       0
                                       indexer
                                       collections)]
      ;; (println (index-to-string expected-index))
      ;; (println (index-to-string (:index actual-indexer)))
      (is (index-to-string expected-index)
          (index-to-string (:index actual-indexer)))
      ;; (is (pl-to-string expected-index) (pl-to-string actual-index))
      )))
