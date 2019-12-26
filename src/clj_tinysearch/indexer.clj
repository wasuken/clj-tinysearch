(ns clj-tinysearch.indexer
  (:require [clj-tinysearch.util :refer :all]
            [clj-tinysearch.tokenizer :refer :all]
            [clj-tinysearch.index :refer :all])
  (:import [java.io ByteArrayInputStream]))

(defprotocol IndexerBase
  (idxr-update [this doc-id reader]))

(defrecord Indexer [index tokenizer]
  IndexerBase
  (idxr-update [this doc-id reader]
    (let [added-indexer (reduce-indexed (fn [idxr i w]
                                          (->Indexer (->Index (merge-with (fn [old new]
                                                                            (add-if-eq-doc-id old (first (:list new))))
                                                                (:dictionary (:index idxr))
                                                                 {w (new-postings-list (new-posting doc-id i))})
                                                              (:total-docs-count (:index idxr)))
                                                     (:tokenizer idxr)))
                                        0
                                        this
                                        (map del-but-e-char->lower-in-str
                                             (clojure.string/split  (slurp reader) #"\s|　")))]
        (->Indexer (->Index (:dictionary (:index added-indexer))
                          (inc (:total-docs-count (:index added-indexer) -1)))
                 (:tokenizer added-indexer)))))

(defn new-indexer [tokenizer]
  (->Indexer (new-index) tokenizer))
