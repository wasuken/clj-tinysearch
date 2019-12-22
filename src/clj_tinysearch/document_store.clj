(ns clj-tinysearch.document-store
  (:require [clj-tinysearch.util :refer :all]
            [clj-tinysearch.tokenizer :refer :all]
            [clojure.java.jdbc :as j]))

(defprotocol DocumentStoreBase
  (save [this title])
  (fetch-title [this doc-id]))

(defrecord DocumentStore [db]
  DocumentStoreBase
  (save [this title]
    (let [result (j/insert! db :documents {"document_title" title})]
      (:generated_key (first result))))
  (fetch-title [this doc-id]
    (:document_title (first (j/query db ["select document_title from documents where document_id = ?" doc-id])))))
