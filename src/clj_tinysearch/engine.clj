(ns clj-tinysearch.engine
  (:require [clj-tinysearch.util :as :all]
            [clj-tinysearch.indexer :refer :all]
            [clj-tinysearch.tokenizer :refer :all]
            [clj-tinysearch.document_store :refer :all]
            [environ.core :refer [env]]))

(defprotocol EngineBase
  (add-document [this title reader])
  (flush [this]))

(defrecord Engine [tokenizer indexer document-store index-dir]
  EngineBase
  (add-document [this title reader]
    (let [id (save (:document-store this) title)]
      (->Engine (:tokenizer this) (idxr-update (:indexer this) id reader)
                (:index-er this))))
  (flush [this]
    (flush (new-index-writer (:index-dir this)) (:index (:indexer e)))))

(defn new-engine [db]
  (let [tk (->Tokenizer)]
    (->Engine tk (new-indexer tk) (new-document-store db)
              (str (. (java.io.File. ".") getCanonicalPath) "/"(env :index-dir-path)))))
