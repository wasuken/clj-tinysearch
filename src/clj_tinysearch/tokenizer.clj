(ns clj-tinysearch.tokenizer
  (:require [clj-tinysearch.util :refer :all]))

(defprotocol TokenizerBase
  (split-func [this data at-eof]))

(defrecord Tokenizer []
    TokenizerBase
  ;; I just don't understand the ae-eof on the golang side
  (split-func [this data at-eof]
    (take (clojure.string/split (String. data) " "))))

;;; delete all but alphabetic characters and convert to lowercase.
;;; return string(is char better?)
(defn del-but-e-char->lower [c]
  (clojure.string/lower-case (if (nil? (re-find (re-pattern #"[a-z|A-Z]") (str c)))
                                                  ""
                                                  (re-find (re-pattern #"[a-z|A-Z]") (str c)))))

(defn del-but-e-char->lower-in-str [s]
  (clojure.string/join (map #(del-but-e-char->lower %) s)))
