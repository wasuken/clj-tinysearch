(ns clj-tinysearch.index-test
  (:require [clojure.test :refer :all]
            [clj-tinysearch.test-util :refer :all]))

(deftest search-top-k-test
  (testing "search processing test"
    (let [s (new-searcher "testdata/index")
          actual (search-top-k s ["quarrel" "sir"] 1)
          expected (new-top-docs 2 [(new-score-doc 2 1.9657842846620868)])]
      (assert-eq actual expected))))
