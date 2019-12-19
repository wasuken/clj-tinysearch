(ns clj-tinysearch.indexer
  (:require [clj-tinysearch.util :refer :all]
            [clj-tinysearch.tokenizer :refer :all]
            [clj-tinysearch.index :refer (->Index)]
            [clj-tinysearch.index :refer [new-posting new-index]])
  (:import [java.io ByteArrayInputStream]))

(defprotocol IndexerBase
  (idxr-update [this doc-id reader]))

(defrecord Indexer [index tokenizer]
  IndexerBase
  (idxr-update [this doc-id reader]
    (let [added-indexer (reduce-indexed (fn [idxr i w]
                                          (->Indexer (merge-with into
                                                                 (:dictionary (:index idxr))
                                                                 {w (new-posting doc-id i)})
                                                     (:tokenizer idxr)))
                                        0
                                        this
                                        (del-but-e-char->lower-in-str (slurp reader)))]
      (->Indexer (->Index (:dictionary (:index added-indexer))
                          (inc (:total-docs-count (:index added-indexer) -1))) ;あとで直せ
                 (:tokenizer added-indexer)))))

(defn new-indexer [tokenizer]
  (->Indexer (new-index) tokenizer))
