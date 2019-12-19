(ns clj-tinysearch.index)


;;; util?
(defn str-compare [^java.lang.String a ^java.lang.String b]
  (if (= (count a) (count b))
    (compare a b)
    (let [a-one (first (take 1 a))
          b-one (first (take 1 b))
          a-minus-b (- (int a-one) (int b-one))]
      (cond (zero? a-minus-b)
            (cond (empty? (drop 1 a)) 1
                  (empty? (drop 1 b)) -1
                  :else (str-compare (clojure.string/join (drop 1 a))
                                     (clojure.string/join (drop 1 b)))
                  )
            :else a-minus-b))))


;;; logic
(defprotocol IndexBase
  (index-to-string [this])
  )

(defprotocol PostingBase
  (posting-to-string [this])
  )

(defprotocol PostingsListBase
  (add [this posting])
  (add-if-eq-doc-id [this posting])
  (list-last [this])
  (pl-to-string [this]))

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
    [^java.lang.Long doc-id ^clojure.lang.ISeq positions ^java.lang.Integer term-frequenc]
  PostingBase
  (posting-to-string [this]
    (format "(%s,%s,%s)" (:doc-id this) (:term-frequenc this) (:positions this))))

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
          (->PostingsList (conj list-remove-last-one updated-last))))))
  (pl-to-string [this]
    (clojure.string/join "=>" (map posting-to-string (:list this)))))

(defn new-postings-list []
  (->PostingsList nil))

(defn new-postings-list [& postings]
  (->PostingsList postings))
