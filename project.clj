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
                 [com.billpiel/sayid "0.0.18"]
                 [cider/cider-nrepl "0.22.4"]
                 [org.clojure/tools.cli "0.4.2"]]
  :plugins [[lein-environ "1.0.0"]
            [refactor-nrepl "2.4.0"]
            [cider/cider-nrepl "0.22.4"]
            [com.billpiel/sayid "0.0.18"]]
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
  :repl-options {:init-ns clj-tinysearch.core
                 :nrepl-middleware
                 [cider.nrepl/wrap-apropos
                  cider.nrepl/wrap-classpath
                  cider.nrepl/wrap-clojuredocs
                  cider.nrepl/wrap-complete
                  cider.nrepl/wrap-debug
                  cider.nrepl/wrap-format
                  cider.nrepl/wrap-info
                  cider.nrepl/wrap-inspect
                  cider.nrepl/wrap-macroexpand
                  cider.nrepl/wrap-ns
                  cider.nrepl/wrap-spec
                  cider.nrepl/wrap-profile
                  cider.nrepl/wrap-refresh
                  cider.nrepl/wrap-resource
                  cider.nrepl/wrap-stacktrace
                  cider.nrepl/wrap-test
                  cider.nrepl/wrap-trace
                  cider.nrepl/wrap-out
                  cider.nrepl/wrap-undef
                  cider.nrepl/wrap-version
                  cider.nrepl/wrap-xref]}
  :main "clj-tinysearch.core")
