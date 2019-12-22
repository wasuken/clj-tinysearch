(ns clj-tinysearch.index-reader
  (:require [clj-tinysearch.util :refer :all]
            [clojure.data.json :as json]))

(defprotocol IndexReaderBase
  (postings-not-empty-lists [this terms])
  (postings [this term])
  (total-doc-count [this]))

(defrecord IndexReader [index-dir postings-cache doc-count-cache]
  IndexReaderBase
  (postings-not-empty-lists [this terms]
    (remove nil? (map #((let [result (:list (postings this %))]
                          (if (empty? result)
                            result)))
                      terms)))
  (postings [this term]
    (if (empty? (get (:postings-cache this) term))
      (get (:postings-cache this) term)
      (let [filename (str (:index-dir this) term)]
        ;; Return value is different from the original.
        (->IndexReader (:index-dir this)
                       (assoc (:postings-cache this) term (json/read-str (slurp filename)))
                       (:doc-count-cache this)))))
  (total-doc-count [this]
    (if (> (:doc-total-cache this) 0)
      (:doc-total-cache this)
      (let [filename (str (:index-dir this) "_0.dc")]
        ;; Return value is different from the original.
        (->IndexReader (:index-dir this)
                       (:postings-cache this)
                       (Integer. (slurp filename)))))))

(defn new-index-reader [path]
  (->IndexReader path {} -1))
