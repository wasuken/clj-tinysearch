(ns clj-tinysearch.searcher
  (:require [clj-tinysearch.util :refer :all]
            [clj-tinysearch.index :refer :all]
            [clj-tinysearch.index-reader :refer :all]))

(defn log2 [n]
  (double (/ (Math/log n) (Math/log 2))))

(defn calc-tf [term-count]
  (if (<= term-count 0)
    0
    (double (+ 1.0 (double (log2 (double term-count)))))))

(defn calc-idf [n df]
  (double (log2 (double (/ (double n) (double df))))))


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
    (let [s-results (sort (comp (fn [x y] (> (:score x) (:score y))))
                              (searcher-search this query-list))
          take-results (take limit s-results)]
      (->TopDocs (count s-results) take-results)))
  (searcher-search [this query-list]
    (let [new-this (atom (open-cursors this query-list))]
      (if (empty? (:cursors @new-this))
        []
        (let [first-crs (first (:cursors @new-this))
              one-drop-crs (atom (drop 1 (:cursors @new-this)))]
          (letfn [(cursor-next-doc [cur doc-id]
                    (if (and (not (nil? cur))
                             (not (nil? (nth (:list (:postings-list cur))
                                             (:pointer cur)
                                             nil)))
                             (< (:doc-id (nth (:list (:postings-list cur))
                                              (:pointer cur)))
                                doc-id))
                      (cursor-next-doc (cur-next cur)
                                       doc-id)
                      cur))
                  (search-next-doc [cur]
                    (let [cur-doc-id (:doc-id (nth (:list (:postings-list cur))
                                                   (:pointer cur)
                                                   nil))
                          find-result (loop [crs-list @one-drop-crs
                                             pointer 0]
                                        (if (not (empty? crs-list))
                                          (let [crs-doc-id (:doc-id (nth (:list (:postings-list (first crs-list)))
                                                                         (:pointer (first crs-list))
                                                                         nil))
                                                next-crs (cursor-next-doc (first crs-list) cur-doc-id)
                                                next-crs-doc-id (:doc-id (nth (:list (:postings-list next-crs))
                                                                              (:pointer next-crs)
                                                                              nil))]
                                            (reset! one-drop-crs (assoc (vec @one-drop-crs) pointer next-crs))
                                            (reset! new-this (->Searcher (:index-reader @new-this)
                                                                          (assoc (vec (:cursors @new-this))
                                                                                 (inc pointer)
                                                                                 next-crs)))
                                            (cond (empty? next-crs)
                                                  {:end true}
                                                  (not (= cur-doc-id next-crs-doc-id))
                                                  {:next-doc-id next-crs-doc-id}
                                                  :else (recur (drop 1 crs-list)
                                                               (inc pointer))))))]
                      (if (empty? find-result)
                        {:next-doc-id 0}
                        find-result)))
                  (find-shortest-posting-list [cur score-docs]
                    (if (empty? cur)
                      score-docs
                      (let [search-result (search-next-doc cur)]
                        (if (:end search-result)
                          score-docs
                          (if (> (:next-doc-id search-result) 0)
                            (let [new-cur (cursor-next-doc cur (:next-doc-id search-result))]
                              (if (or (empty? new-cur)
                                      (empty? (nth (:list (:postings-list new-cur))
                                                   (:pointer new-cur)
                                                   nil)))
                                score-docs
                                (find-shortest-posting-list new-cur score-docs)))
                            (find-shortest-posting-list (cur-next cur)
                                                        (conj score-docs
                                                              (->ScoreDoc (:doc-id (nth (:list (:postings-list cur))
                                                                                        (:pointer cur)))
                                                                          (calc-score @new-this)))))))))]
            (find-shortest-posting-list first-crs [])))))
    )
  ;; return value is different from origininal.
  (open-cursors [this query-list]
    (let [result-map (postings-not-empty-lists (:index-reader this) query-list)]
      (if (zero? (count (:pls result-map)))
        this
        (->Searcher (:index-reader result-map)
                    (map #(open-cursor %)
                         (sort (comp (fn [x y] (< (count x) (count y))))
                               (:pls result-map)))))))
  (calc-score [this]
    (reduce (fn [score cursor]
              (let [term-freq (:term-frequency (nth (:list (:postings-list cursor))
                                                    (:pointer cursor)
                                                    nil))
                    doc-count (count (:list (:postings-list cursor)))
                    tdc (:doc-count-cache (total-doc-count (:index-reader this)))]
                (+ score (* (calc-tf term-freq) (calc-idf tdc doc-count)))))
            0.0
            (:cursors this))))

(defn new-searcher [path]
  (->Searcher (new-index-reader path) []))
