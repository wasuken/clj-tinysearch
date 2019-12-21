(defproject clj-tinysearch "0.1.0-SNAPSHOT"
  :description "study search engine"
  :url "https://github.com/wasuken/clj-tinysearch"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [environ "1.1.0"]
                 [org.clojure/java.jdbc "0.7.10"]
                 [mysql/mysql-connector-java "8.0.18"]
                 [org.clojure/data.json "0.2.7"]
                 [eftest "0.5.9"]]
  :plugins [[lein-environ "1.0.0"]]
  :profiles {:dev        {:env {:db-classname "com.mysql.cj.jdbc.Driver"
                                :db-type "mysql"
                                :db-name "tinysearch"
                                :db-user "root"
                                :db-host "127.0.0.1"
                                :db-password ""}}
             :test       {:env {:db-classname "com.mysql.cj.jdbc.Driver"
                                :db-type "mysql"
                                :db-name "tinysearch"
                                :db-user "root"
                                :db-host "127.0.0.1"
                                :db-password ""}}
             :production {:env {:db-classname "com.mysql.cj.jdbc.Driver"
                                :db-type "mysql"
                                :db-name "tinysearch"
                                :db-user "root"
                                :db-host "127.0.0.1"
                                :db-password ""}}}
  :repl-options {:init-ns clj-tinysearch.core}
  :main "clj-tinysearch.core")
