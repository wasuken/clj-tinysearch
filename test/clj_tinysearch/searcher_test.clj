(ns clj-tinysearch.searcher-test
  (:require [clojure.test :refer :all]
            [environ.core :refer [env]]
            [clj-tinysearch.searcher :refer :all]))

;; (defn top-docs-almost-match [a e]
;;   (cond (= a b)
;;         (is true)
;;         ()))

(deftest search-top-k-test
  (testing "search processing test"
    (let [s (new-searcher (env :search-index-dir-path))
          actual (search-top-k s ["quarrel" "sir"] 1)
          expected (->TopDocs 2 [(->ScoreDoc 2 1.9657842846620868)])]
      (is (= actual expected)))))
