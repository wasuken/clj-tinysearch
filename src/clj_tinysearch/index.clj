(ns clj-tinysearch.index)

(defprotocol IndexBase
  )

(defprotocol PostingBase
  )

(defprotocol PostingsListBase
  (add [this posting])
  (add-if-eq-doc-id [this posting])
  (list-last [this]))

(defrecord Index
    [^clojure.lang.IPersistentMap dictionary ^java.lang.Long total-docs-count]
  IndexBase
  )

(defn new-index [] (->Index {} 0))

(defrecord Posting
    [^java.lang.Long doc-id ^clojure.lang.ISeq positions ^java.lang.Integer term-frequenc]
  PostingBase)

(defn new-posting [^java.lang.Long doc-id ^clojure.lang.ISeq positions]
  (->Posting doc-id positions (count positions)))

(defrecord PostingsList [list]
  PostingsListBase
  (add [this posting]
    (->PostingsList (conj (:list this) posting)))
  (list-last [this] (last (:list this)))
  (add-if-eq-doc-id [this posting]
    (let [last (list-last this)
          list-remove-last-one (reverse (drop 1 (reverse (:list this))))]
      (if (or (empty (:list this))
              (not (= (:doc-id (list-last this)) (:doc-id posting))))
        (add this posting)
        (let [updated-last (->Posting (+ (:doc-id) 1)
                                      (concat (:positions last) (:positions posting)))]
          (->PostingsList (conj list-remove-last-one updated-last)))))))

(defn new-postings-list []
  (->PostingsList nil))

(defn new-postings-list [& postings]
  (->PostingsList postings))
