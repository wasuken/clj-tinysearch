(ns clj-tinysearch.core
  (:require [clj-tinysearch.index :refer :all]))

(defn -main
  "I don't do a whole lot."
  []
  (let [p (new-posting 1 [1 2 3])
        p2 (new-posting 2 (range 10 20))
        p3 (new-posting 2 (range 10 20))
        pl (new-postings-list p p2 p3)
        pl2 (new-postings-list p3 p p2)
        idx (->Index {"test" pl, "hoge" pl2} 2)]
    (println (index-to-string idx))))
