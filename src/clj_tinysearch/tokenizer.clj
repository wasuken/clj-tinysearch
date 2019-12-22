(ns clj-tinysearch.tokenizer
  (:require [clj-tinysearch.util :refer :all]))

;;; delete all but alphabetic characters and convert to lowercase.
;;; return string(is char better?)
(defn del-but-e-char->lower [c]
  (clojure.string/lower-case (if (nil? (re-find (re-pattern #"[a-z|A-Z]") (str c)))
                               ""
                               (re-find (re-pattern #"[a-z|A-Z]") (str c)))))

(defn del-but-e-char->lower-in-str [s]
  (clojure.string/join (map #(del-but-e-char->lower %) s)))

;; I just don't understand the ae-eof on the golang side
(defn text->word-seq [text]
  (map del-but-e-char->lower-in-str
       (clojure.string/split text #"\s|　")))

(defn split-func [this data at-eof]
  (clojure.string/split (String. data) " "))

(defprotocol TokenizerBase)

(defrecord Tokenizer []
  TokenizerBase)
