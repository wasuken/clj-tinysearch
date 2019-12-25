(ns clj-tinysearch.index
  (:require [clj-tinysearch.util :refer :all]
            [clojure.data.json :as json]))

;;; logic
(defprotocol IndexBase
  (index-to-string [this])
  )

(defprotocol PostingBase
  (posting-to-string [this]))

(defprotocol PostingsListBase
  (add [this posting])
  (add-if-eq-doc-id [this posting])
  (list-last [this])
  (pl-to-string [this])
  (to-json-string [this])
  (json-string-add [this json-string])
  (open-cursor [this]))

(defprotocol CursorBase
  (cur-to-string [this])
  (cur-next [this]))

(defrecord Cursor [postings-list pointer]
  CursorBase
  (cur-to-string [this]
    (posting-to-string (:current this)))
  (cur-next [this]
    (if (nil? (nth (:list (:postings-list this))
                   (inc (:pointer this))
                   nil))
      nil
      (->Cursor (:postings-list this)
              (inc (:pointer this))))))

(defrecord Index
    [^clojure.lang.IPersistentMap dictionary ^java.lang.Long total-docs-count]
  IndexBase
  (index-to-string [this]
    (let [max-size (reduce (fn [max-cnt v] (max max-cnt (count v)))
                           0 (keys (:dictionary this)))
          sorted-keys (sort str-compare (keys dictionary))
          fmt-material (str "[%-" max-size "s] -> %s")
          dic-all-str (map (fn [x] (format fmt-material x
                                           (pl-to-string (get dictionary x))))
                           (filter #(contains? dictionary %) sorted-keys))]
      (format "total documents : %s\ndictionary:\n%s\n"
              (:total-docs-count this)
              (clojure.string/join "\n" dic-all-str)))))

(defn new-index [] (->Index {} 0))

(defrecord Posting
    [^java.lang.Long doc-id ^clojure.lang.ISeq positions ^java.lang.Integer term-frequency]
  PostingBase
  (posting-to-string [this]
    (format "(%s,%s,%s)" (:doc-id this) (:term-frequency this) (pr-str (:positions this)))))

(defn new-posting [^java.lang.Long doc-id & positions]
  (->Posting doc-id positions (count positions)))

(defrecord PostingsList [list]
  PostingsListBase
  (add [this posting]
    (->PostingsList (concat (:list this) [posting])))
  (list-last [this] (last (:list this)))
  (add-if-eq-doc-id [this posting]
    (let [list-remove-last-one (reverse (drop 1 (reverse (:list this))))]
      (if (or (empty? (list-last this))
              (not (= (:doc-id (list-last this))
                      (:doc-id posting))))
        (add this posting)
        (let [updated-last (->Posting (:doc-id posting)
                                      (concat (:positions (list-last this)) (:positions posting))
                                      (inc (:term-frequency posting)))]
          (->PostingsList (concat list-remove-last-one [updated-last]))))))
  (pl-to-string [this]
    (clojure.string/join "=>" (map posting-to-string (:list this))))
  (to-json-string [this]
    (json/write-str (:list this)))
  (json-string-add [this json-string]
    (reduce (fn [pl x] (add pl x))
            this
            (json/read-str json-string)))
  (open-cursor [this]
    (->Cursor this 0)))

(defn new-postings-list []
  (->PostingsList nil))

(defn new-postings-list [& postings]
  (->PostingsList postings))
