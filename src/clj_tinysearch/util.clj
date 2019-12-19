(ns clj-tinysearch.util)

(defn reduce-indexed
  ([f i val call] (cond (empty? call) val
                        :else (reduce-indexed f (inc i) (f val i (first call)) (drop 1 call)))))
