(ns clj-tinysearch.engine
  (:require [clj-tinysearch.util :refer :all]
            [clj-tinysearch.indexer :refer :all]
            [clj-tinysearch.tokenizer :refer :all]
            [clj-tinysearch.index-writer :refer :all]
            [clj-tinysearch.document-store :refer :all]
            [clj-tinysearch.searcher :refer :all]
            [environ.core :refer [env]]))

(defprotocol EngineBase
  (add-document [this title reader])
  (e-flush [this])
  (engine-search [this query limit]))

(defrecord SearchResult [doc-id score title])

(defrecord Engine [tokenizer indexer document-store index-dir]
  EngineBase
  (add-document [this title reader]
    (let [id (save (:document-store this) title)]
      (->Engine (:tokenizer this)
                (idxr-update (:indexer this) id reader)
                (:document-store this)
                (:index-dir this))
      ))
  (e-flush [this]
    (iw-flush (->IndexWriter (:index-dir this)) (:index (:indexer this))))
  (engine-search [this query limit]
    (map #(->SearchResult (:doc-id %)
                          (:score %)
                          (fetch-title (:document-store this) (:doc-id %)))
         (search-top-k (new-searcher (:index-dir this))
                       (text->word-seq query)
                       limit))))

(defn new-engine [db]
  (let [tk (->Tokenizer)]
    (->Engine tk
              (new-indexer tk)
              (->DocumentStore db)
              (if (empty? (env :index-dir-path))
                (str (. (java.io.File. ".") getCanonicalPath) "/_index_data/")
                (env :index-dir-path)))))
