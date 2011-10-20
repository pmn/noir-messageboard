(ns noir-messageboard.utils.migrations
  (:require [clojure.java.jdbc :as jdbc]
            [noir-messageboard.utils.db :as db])
  (:import org.postgresql.Driver))

;; Table initialization.
(defn init-user-table! []
  (jdbc/with-connection db/db
    (jdbc/create-table
     :users
     [:id "serial primary key"]
     [:username "varchar"]
     [:password "varchar"]
     [:essay "varchar"]
     [:email "varchar"]
     [:createdat :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])))

(defn init-post-table! []
  (jdbc/with-connection db/db
    (jdbc/create-table
     :posts
     [:id "serial primary key"]
     [:ownerid :int "references users(id)"]
     [:title "varchar"]
     [:body "varchar"]
     [:createdat :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])))

(defn init-comment-table! []
  (jdbc/with-connection db/db
    (jdbc/create-table
     :comments
     [:id "serial primary key"]
     [:postid :int "references posts(id)"]
     [:parentid :int]
     [:body "varchar"]
     [:ownerid :int "references users(id)"]
     [:createdat :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])))

(defn init-db! []
  (init-user-table!)
  (init-post-table!)
  (init-comment-table!))

(defn -main []
  (print "Creating tables... ")(flush)
  (init-db!)
  (println "... done!"))
     