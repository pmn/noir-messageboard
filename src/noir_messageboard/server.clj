(ns noir-messageboard.server
  (:use [ring.middleware.session.cookie :only (cookie-store)])
  (:require [noir.server :as server]))

(server/load-views "src/noir_messageboard/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'noir-messageboard
                        :session-store (cookie-store)})))

