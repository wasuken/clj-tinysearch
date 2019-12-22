(ns clj-tinysearch.searcher
  (:require [clj-tinysearch.util :refer :all]
            [clj-tinysearch.index :refer :all]
            [clj-tinysearch.index-reader :refer :all]))

(defn calc-tf [term-count]
  (if (<= term-count 0)
    0
    (+ 1 (Math/log (double term-count)))))

(defn calc-idf [n df]
  (Math/log (/ (double n) (double df))))


(defprotocol TopDocsBase
  (tds-to-string [this]))

(defprotocol ScoreDocBase
  (sd-to-string [this]))

(defprotocol SearcherBase
  (search-top-k [this query-list limit])
  (searcher-search [this query-list])
  (open-cursors [this query-list])
  (calc-score [this]))

(defrecord TopDocs [total-hits score-docs]
  TopDocsBase
  (tds-to-string [this]
    (format "\ntotal hits: %v\nresults: %v\n" (:total-hits this) (:score-docs this))))

(defrecord ScoreDoc [doc-id score]
  ScoreDocBase
  (sd-to-string [this]
    (format "docId: %v, Score: %v" (:doc-id this) (:score this))))

(defrecord Searcher [index-reader cursors]
  SearcherBase
  (search-top-k [this query-list limit]
    (let [results (take limit (sort (comp (fn [x y] (> (:score x) (:score y))))
                                    (searcher-search this query-list)))]
      (->TopDocs (count results) results)))
  (searcher-search [this query-list]
    (let [new-this (open-cursors this query-list)]
      (if (empty? (:cursors new-this))
        []
        (let [first-crs (first (:cursors new-this))
              one-drop-crs (drop 1 (:cursors new-this))]
          (letfn [(cursor-next-doc [cur doc-id]
                    (if (and (not (nil? cur))
                             (not (nil? (nth (:postings-list cur) (:pointer cur))))
                             (< (:pointer cur) doc-id))
                      (cursor-next-doc (nth (:postings-list cur)
                                            (inc (:pointer cur)))
                                       doc-id)
                      cur))
                  (search-next-doc [cur]
                    (find-if #(cond (empty? (cursor-next-doc %))
                                    (reduced {:end true})
                                    (not (= (:doc-id cur) (:doc-id %)))
                                    (reduced {:next-doc-id (:doc-id cur)}))
                             one-drop-crs))
                  (find-shortest-posting-list [cur score-docs]
                    (let [search-result (search-next-doc cur)]
                      (if (or (empty? cur)
                              (:end search-result))
                        score-docs
                        (if (> (:next-doc-id search-result) 0)
                          (let [new-cur (search-next-doc cur)]
                            (if (empty? new-cur)
                              score-docs
                              (find-shortest-posting-list new-cur score-docs)))
                          (find-shortest-posting-list (cur-next cur)
                                                      (conj score-docs
                                                            (->ScoreDoc (:doc-id cur)
                                                                        (:calc-score this))))))))]
            (find-shortest-posting-list first-crs)))))
    )
  ;; return value is different from origininal.
  (open-cursors [this query-list]
    (let [pls (postings-not-empty-lists (:index-reader this) query-list)]
      (if (zero? (count pls))
        this
        (->Searcher (:index-reader)
                    (sort (comp (fn [x y] (< (count x) (count y))))
                          pls)))))
  (calc-score [this]
    (reduce (fn [score cursor]
              (let [term-freq (:term-frequency (:term-frequency (nth (:list (:postings-list cursor))
                                                                     (:pointer cursor))))
                    doc-count (count (:posting-list cursor))
                    tdc (total-doc-count (:index-reader this))]
                (* (calc-tf term-freq) (calc-idf tdc doc-count))))
            0.0
            (:cursors this))))

(defn new-searcher [path]
  (->Searcher (new-index-reader path) []))

