(defproject noir-messageboard "0.1.0-SNAPSHOT"
            :description "Noir Messageboard: A messageboard created with Noir"
            :dependencies [[org.clojure/clojure "1.2.1"]
                           [noir "1.1.0"]
                           [clj-time "0.3.0"]
                           [org.clojure/java.jdbc "0.0.6"]
                           [org.markdownj/markdownj "0.3.0-1.0.2b4"]
                           [postgresql/postgresql "8.4-701.jdbc4"]]
            :main noir-messageboard.server)

