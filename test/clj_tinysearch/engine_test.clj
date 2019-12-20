(ns clj-tinysearch.engine-test
  (:require [clojure.test :refer :all]
            [clj-tinysearch.index :refer :all]
            [clj-tinysearch.util :refer :all]
            [clj-tinysearch.engine :refer :all]
            [clojure.java.jdbc :as j]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(def mysql-db (env :database-url))

(defn setup []
  (do
    (j/execute! mysql-db ["truncate table documents"])
    (remove-dir-all "_index_data")
    (clojure.java.io/make-parents "_index_data/a")
    ))

;;; Should I not use it.
;; (defrecord testDoc [^java.lang.String title ^java.lang.String body])

(deftest create-index-test
  (testing "Testing the index building process"
    (let [docs [{"test1" "Do you quarrel, sir?"}
                {"test2" "No better."}
                {"test3" "Quarrel sir! no, sir!"}]
          se (new-engine mysql-db)
          added-se (reduce (fn [se [k v]]
                             (add-document se k (java.io.ByteArrayInputStream (.getBytes v))))
                           docs)
          flushed (search-engine-flush added-se)
          test-cases [{:file "_index_data/_0.dc" :postingsStr "3"}
                      {:file "_index_data/better" :postingsStr "[{\"DocID\":2,\"Positions\":[1],\"TermFrequency\":1}]"}
                      {:file "_index_data/no" :postingsStr "[{\"DocID\":2,\"Positions\":[0],\"TermFrequency\":1}
{\"DocID\":3,\"Positions\":[2],\"TermFrequency\":1}]"}
                      {:file "_index_data/do" :postingsStr "[{\"DocID\":1,\"Positions\":[0],\"TermFrequency\":1}]"}
                      {:file "_index_data/quarrel" :postingsStr "[{\"DocID\":1,\"Positions\":[2],\"TermFrequency\":1}
{\"DocID\":3,\"Positions\":[0],\"TermFrequency\":1}]"}
                      {:file "_index_data/sir" :postingsStr "[{\"DocID\":1,\"Positions\":[3],\"TermFrequency\":1}
{\"DocID\":3,\"Positions\":[1,3],\"TermFrequency\":2}]"}
                      {:file "_index_data/you" :postingsStr "[{\"DocID\":1,\"Positions\":[1],\"TermFrequency\":1}]"}]]
      (doseq [test-case test-cases]
        (let [read-file-json (json/read-str (slurp (:file test-case)))
              test-case-json (json/read-str (:postingsStr test-case))]
          (is read-file-json test-case-json))))))
