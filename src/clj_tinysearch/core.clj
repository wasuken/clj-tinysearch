(ns clj-tinysearch.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clj-tinysearch.cmd.create-index :refer :all]
            [clj-tinysearch.cmd.search :refer :all]
            [clj-tinysearch.engine :refer :all]
            [environ.core :refer [env]])
  (:gen-class))


(def cli-options
  ;; 引数が必要なオプション
  [["-n" "--limit NUMBER" "Port number"
    :default 10
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 %) "Must be a number between 0"]]])

(defn -main
  [& args]
  (let [parsed-opt (parse-opts args cli-options)
        arguments (:arguments parsed-opt)
        engine (new-engine {:classname (env :db-classname)
                            :dbtype (env :db-type)
                            :dbname (env :db-name)
                            :user (env :db-user)
                            :host (env :db-host)
                            :port (env :db-port)
                            :password (env :db-password)})]
    (cond (zero? (count arguments))
          (println "not arguments")
          (and (= "create" (first arguments)) (= (count arguments) 2))
          (create-index engine (nth arguments 1))
          (= "search" (first arguments))
          (search engine
                  (clojure.string/join " " (drop 1 arguments))
                  (:limit (:options parsed-opt)))
          :else (println "failed command"))
    )
  )
