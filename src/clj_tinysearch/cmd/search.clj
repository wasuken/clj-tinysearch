(ns clj-tinysearch.cmd.search
  (:require [clj-tinysearch.engine :refer :all]))

(defn print-result [results]
  (if (<= (count results) 0)
    (println "0 match!!")
    (clojure.string/join "\n"
                         (map-indexed (fn [i x]
                                        (format "rank: %3d, score: %4f, title: %s"
                                                (inc i)
                                                (:score x)
                                                (:title x)))
                                      results))))

(defn search [engine query limit]
  (println (print-result (engine-search engine query limit))))
