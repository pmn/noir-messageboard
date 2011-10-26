(ns noir-messageboard.utils.addstickyfield
  (:require [clojure.java.jdbc :as jdbc]
            [noir-messageboard.utils.db :as db])
  (:import org.postgresql.Driver))

(defn update-fields
  "Add the 'stickied' field to the posts table"
  []
  (let [sql "UPDATE posts SET stickied = false"]
    (jdbc/with-connection db/db
      (jdbc/do-commands sql))))

(defn -main []
  (print "running migration... ") (flush)
  (update-fields)
  (println "done!"))