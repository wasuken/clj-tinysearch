(ns clj-tinysearch.document_store
  (:require [clj-tinysearch.util :refer :all]
            [clj-tinysearch.tokenizer :refer :all]
            [clj-tinysearch.index :refer (->Index)]
            [clj-tinysearch.index :refer [new-posting new-index]]
            [clojure.java.jdbc "as" j]))

(defprotocol DocumentStoreBase
  (save [this title]))

(defrecord DocumentStore [db]
  DocumentStoreBase
  (save [this title]
    (let [result (j/insert! db :documents [title])]
      (pritln result))))                ;必ずこける。けどresultの内容はみれる。
