(ns clj-tinysearch.cmd.create-index
  (:require [clj-tinysearch.util :refer :all]
            [clj-tinysearch.engine :refer :all]
            [clj-tinysearch.cmd.search :refer :all]))

(defn base-name [filepath]
  (clojure.string/replace (.getName (clojure.java.io/file filepath))
                          #"\..*"
                          ""))

(defn add-files [engine filepath]
  (let [title (base-name filepath)
        new-engine (add-document engine
                                 title
                                 (java.io.ByteArrayInputStream.
                                  (.getBytes (slurp filepath))))]
    (println (format "add document to index: %s\n" title))
    new-engine))

(defn fetch-files [dirpath]
  (map #(.getAbsolutePath %)
       (remove #(or (not (re-matches #".*\.txt$" (.getName %)))
                    (.isDirectory %))
               (file-seq (clojure.java.io/file dirpath)))))

(defn create-index [engine dirpath]
  (e-flush (reduce (fn [e x] (add-files e x))
                   engine
                   (fetch-files dirpath))))
