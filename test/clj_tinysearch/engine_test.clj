(ns clj-tinysearch.engine-test
  (:require [clojure.test :refer :all]
            [clj-tinysearch.index :refer :all]
            [clj-tinysearch.test-util :refer :all]
            [clj-tinysearch.util :refer :all]
            [clj-tinysearch.engine :refer :all]
            [clj-tinysearch.index-writer :refer :all]
            [clojure.java.jdbc :as j]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(def mysql-db {:classname (env :db-classname)
               :dbtype (env :db-type)
               :dbname (env :db-name)
               :user (env :db-user)
               :host (env :db-host)
               :port (env :db-port)
               :password (env :db-password )})

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
    (do
      (setup)
      (let [docs [{:title "test1", :body "Do you quarrel, sir?"}
                  {:title "test2", :body "No better."}
                  {:title "test3", :body "Quarrel sir! no, sir!"}]
            se (new-engine mysql-db)
            added-se (reduce (fn [se doc]
                               (add-document se
                                             (:title doc)
                                             (java.io.ByteArrayInputStream. (.getBytes (:body doc)))))
                             se
                             docs)
            flushed (e-flush added-se)
            test-cases [{:file "_index_data/_0.dc" :postingsStr "3"}
                        {:file "_index_data/better" :postingsStr "[{\"doc-id\":2,\"positions\":[1],\"term-frequency\":1}]"}
                        {:file "_index_data/no" :postingsStr "[{\"doc-id\":2,\"positions\":[0],\"term-frequency\":1}
{\"doc-id\":3,\"positions\":[2],\"term-frequency\":1}]"}
                        {:file "_index_data/do" :postingsStr "[{\"doc-id\":1,\"positions\":[0],\"term-frequency\":1}]"}
                        {:file "_index_data/quarrel" :postingsStr "[{\"doc-id\":1,\"positions\":[2],\"term-frequency\":1}
{\"doc-id\":3,\"positions\":[0],\"term-frequency\":1}]"}
                        ;; ここのパース結果が致命的に違う。
                        {:file "_index_data/sir" :postingsStr "[{\"doc-id\":1,\"positions\":[3],\"term-frequency\":1}
{\"doc-id\":3,\"positions\":[1,3],\"term-frequency\":2}]"}
                        {:file "_index_data/you" :postingsStr "[{\"doc-id\":1,\"positions\":[1],\"term-frequency\":1}]"}]]
        (doseq [test-case test-cases]
          (let [read-file-json (json/read-str (slurp (:file test-case)))
                test-case-json (json/read-str (:postingsStr test-case))]
            (assert-eq read-file-json test-case-json)))))))

(deftest search-test
  (testing "search test"
    (let [e (new-engine mysql-db)
          query "Quarrel, sir."
          actual (engine-search e query 5)
          expected [(->SearchResult 3 1.754887502163469 "test3")
                    (->SearchResult 1 1.1699250014423126 "test1")]]
      (assert-eq actual expected))))
