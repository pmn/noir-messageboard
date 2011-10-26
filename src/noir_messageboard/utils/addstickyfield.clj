(ns noir-messageboard.utils.addstickyfield
  (:require [clojure.java.jdbc :as jdbc]
            [noir-messageboard.utils.db :as db])
  (:import org.postgresql.Driver))

(defn create-sticky-field
  "Add the 'stickied' field to the posts table"
  []
  (let [sql "ALTER TABLE posts ADD stickied BOOLEAN"]
    (jdbc/with-connection db/db
      (jdbc/do-commands sql))))

(defn -main []
  (print "running migration... ") (flush)
  (create-sticky-field)
  (println "done!"))