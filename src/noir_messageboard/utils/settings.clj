(ns noir-messageboard.utils.settings
  (:require [clojure.string :as str])
  (:import (java.net URI)))


(defn smtp-resource
  "Get smtp server information"
  []
  (let [server (System/getenv "SMTP_SERVER")
        port (System/getenv "SMTP_PORT")
        authentication "plain"
        username (System/getenv "SMTP_USERNAME")
        password (System/getenv "SMTP_PASSWORD")
        domain (System/getenv "SMTP_DOMAIN")]
    {:server server
     :port port
     :authentication authentication
     :username username
     :password password
     :domain domain}))

(defn secret-key 
  "Generate a secret key"
  []
  (System/getenv "SECRET_KEY"))