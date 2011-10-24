(ns noir-messageboard.models.user
  (:use [hiccup.core :only [escape-html]])
  (:require [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir-messageboard.utils.db :as db]
            [noir-messageboard.utils.utils :as utils]))

(defn get-by-username [username]
  (first
   (db/fetch-results
    ["SELECT id, username, password, essay, email
      FROM users
      WHERE username = ?"
     username])))

(defn get-by-id [id]
  (first
   (db/fetch-results
    ["SELECT id, username, password, essay, email,
             (select count(*) from posts where userid = ?) as postcount,
             (select count(*) from comments where userid = ?) as commentcount
      FROM users
      WHERE id = ?"
     (Integer/parseInt id)
     (Integer/parseInt id)
     (Integer/parseInt id)])))

(defn get-list []
  (db/fetch-results
   ["SELECT u.*,
          (select count(*) from posts where ownerid = u.id) as postcount,
          (select count(*) from comments where ownerid = u.id) as commentcount
     FROM users u
     ORDER BY createdat DESC"]))

(defn get-posts [userid]
  (db/fetch-results
   ["SELECT *
     FROM posts
     WHERE ownerid = ?
     ORDER BY createdat DESC"
    userid]))

(defn is-admin? [userid]
  "Test if the user is an administrator. Currently the first user is the admin."
  (= userid (->
             (db/fetch-results
              ["SELECT MIN(id) as minid FROM users"])
             first
             :minid)))

;; Validation

(defn exists?
  "Test if the username already exists"
  [username]
  (< 0 (->
        (db/fetch-results
         ["SELECT COUNT(*)
           FROM users
           WHERE username = ?"
          username])
         first
         :count)))

(defn password-correct?
  "Check if the user has provided the correct password."
  [{:keys [username password]}]
  (when-let [storedpass (-> username get-by-username :password)]
    (crypt/compare password storedpass)))

(defn valid?
  "Test if the data recieved is valid"
  [{:keys [username password confirmpass]}]
  (vali/rule (not (exists? username))
             [:username "This username has already been reserved."])
  (vali/rule (vali/has-value? username)
             [:username "Please select a username."])
  (vali/rule (vali/has-value? password)
             [:password "Password is required."])
  (vali/rule (= password confirmpass)
             [:confirmpass "Password and confirmation do not match."])
  (not (vali/errors? :username :password :confirmpass)))

(defn valid-profile? [{:keys [email]}]
  ;; email is an optional field
  (vali/rule (or (= "" email)
                 (vali/is-email? email))
             [:email "Not a valid email address."])
  (not (vali/errors? :email)))

(defn passwords-match?
  [{:keys [password confirmpass]}]
  (vali/rule (= password confirmpass)
             [:confirmpass "Password and confirmation do not match."])
  (not (vali/errors? :confirmpass)))

;; Auth

(defn can-login?
  "Determine if the user may log in."
  [login]
  (vali/rule (password-correct? login)
             [:password "Password incorrect"])
  (not (vali/errors? :password)))

(defn login
  "Attempt to log the user in"
  [user]
  (when (can-login? user)
    (session/put! :user (get-by-username (:username user)))
    (session/flash-put! "Successfully signed in.")
    user))

(defn logout
  "Log the user out."
  []
  (session/clear!))

(defn logged-in? []
  (session/get :user))

(defn current-user-id []
  (:id (session/get :user)))

;; CRUD

(defn add! [{:keys [username password] :as user}]
  (let [usermap {:username (escape-html username)
                 :password (utils/with-crypted password)}]
    (when (valid? user)
      (db/insert! :users
                  usermap))))

(defn delete!
  "Delete a user AND all of the user's posts"
  [userid]
  (db/delete-by-ownerid! :comments (Integer/parseInt userid))
  (db/delete-by-ownerid! :posts (Integer/parseInt userid))
  (db/delete! :users (Integer/parseInt userid)))

(defn save-profile!
  "Save the user's profile."
  [profile]
  (when (valid-profile? profile)
    (db/update! :users
                (:id (session/get :user))
                profile)
    ; refresh the user's session with the updated values
    (session/put! :user (get-by-username (:username (session/get :user))))
    profile))

(defn change-password!
  "Change a user's password."
  [{:keys [password confirmpass] :as changedpassword}] 
  (when (passwords-match? changedpassword)
    (db/update! :users
                (:id (session/get :user))
                {:password (utils/with-crypted password)})))