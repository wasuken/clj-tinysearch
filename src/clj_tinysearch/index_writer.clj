(ns clj-tinysearch.index-writer
  (:require [clj-tinysearch.util :refer :all]
            [clj-tinysearch.index :refer :all]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(defprotocol IndexWriterBase
  (iw-flush [this index])
  (w-postings-list [this term lst])
  (doc-count [this cnt]))

(defrecord IndexWriter [index-dir]
  IndexWriterBase
  (iw-flush [this index]
    (do
      (doall (map (fn [[k v]]
                    (w-postings-list this k v))
                  (:dictionary index)))
      (doc-count this (:total-docs-count index))))
  (w-postings-list [this term lst]
    (let [lst-json-str (to-json-string lst)
          filepath (str (:index-dir this) term)]
      (spit filepath lst-json-str)))
  (doc-count [this cnt]
    (let [filepath (str (:index-dir this)  "_0.dc")]
      (spit filepath cnt))))
