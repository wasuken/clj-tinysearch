(ns clj-tinysearch.util)

(defn reduce-indexed
  ([f i val call] (cond (empty? call) val
                        :else (reduce-indexed f (inc i) (f val i (first call)) (drop 1 call)))))

(defn remove-dir-all [path]
  "danger"
  (do
    (map #(clojure.java.io/delete-file (.getPath %))
         (filter #(.isFile %)
                 (file-seq (clojure.java.io/file path))))
    (map #(clojure.java.io/delete-file (.getPath %))
         (reverse (file-seq (clojure.java.io/file path))))))
