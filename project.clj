(defproject clj-tinysearch "0.1.0-SNAPSHOT"
  :description "study search engine"
  :url "https://github.com/wasuken/clj-tinysearch"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [mysql/mysql-connector-java "8.0.18"]]
  :repl-options {:init-ns clj-tinysearch.core}
  :main "clj-tinysearch.core")
