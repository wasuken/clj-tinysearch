(ns clj-tinysearch.index-reader
  (:require [clj-tinysearch.util :refer :all]
            [clj-tinysearch.index :refer :all]
            [clojure.data.json :as json]))

(defprotocol IndexReaderBase
  (postings-not-empty-lists [this terms])
  (postings [this term])
  (total-doc-count [this]))

(defn json-read-str->postings-list-seq [str]
  (->PostingsList (map #(->Posting (get % "doc-id")
                                   (get % "positions")
                                   (get % "term-frequency"))
                       (json/read-str str))))

(defrecord IndexReader [index-dir postings-cache doc-count-cache]
  IndexReaderBase
  (postings-not-empty-lists [this terms]
    (reduce (fn [map term]
              (let [reader (postings (:index-reader map) term)]
                {:index-reader reader
                 :pls (conj (:pls map)
                            (get (:postings-cache reader) term))
                 }
                ))
            {:index-reader this
             :pls []}
            terms))
  (postings [this term]
    (if (not (empty? (get (:postings-cache this) term)))
      this
      (let [filename (str (:index-dir this) term)]
        ;; Return value is different from the original.
        (->IndexReader (:index-dir this)
                       (assoc (:postings-cache this)
                              term
                              (json-read-str->postings-list-seq (slurp filename)))
                       (:doc-count-cache this)))))
  (total-doc-count [this]
    (if (> (:doc-count-cache this) 0)
      this
      (let [filename (str (:index-dir this) "_0.dc")]
        ;; Return value is different from the original.
        (->IndexReader (:index-dir this)
                       (:postings-cache this)
                       (Integer. (clojure.string/replace (slurp filename) #"\s" "")))))))

(defn new-index-reader [path]
  (->IndexReader path {} -1))
