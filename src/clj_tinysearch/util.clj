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

(defn find-if [f call]
  (reduce (fn [x y] (if (f y)
                      (reduced y)
                      nil))
          nil
          call))

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
