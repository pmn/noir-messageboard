(ns noir-messageboard.utils.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str])
  (:import [org.postgresql.Driver]
           [java.net URI]))

(defn database-resource
  "Get database connection information from ENV.
   SITE_DATABASE_URL should look like:
   postgresql://username:password@localhost:5432/dbname"
  []
  (let [url (URI. (System/getenv "SITE_DATABASE_URL"))
        host (.getHost url)
        port (if (pos? (.getPort url)) (.getPort url) 5432)
        path (.getPath url)]
    (merge
     {:subname (str "//" host ":" port path)}
     (if-let [user-info (.getUserInfo url)]
       {:user (first (str/split user-info #":"))
        :password (second (str/split user-info #":"))}))))

(def db 
  (merge
   {:classname "org.postgresql.Driver"
    :subprotocol "postgresql"}
   (database-resource)))

(defmacro with-db [& body]
  `(jdbc/with-connection db ~@body))

(defn insert! [tablename values]
  (with-db
    (jdbc/insert-records tablename values)))

(defn update! [tablename id values]
  (with-db
    (jdbc/update-values
     tablename
     ["id = ?" id]
     values)))

(defn delete! [tablename id]
  (with-db
    (jdbc/delete-rows
     tablename
     ["id = ?" id])))

(defn delete-by-ownerid! [tablename ownerid]
  (with-db
    (jdbc/delete-rows
     tablename
     ["ownerid = ?" ownerid])))

(defn fetch-results [query]
  (with-db
    (jdbc/with-query-results res query
      (doall res))))
