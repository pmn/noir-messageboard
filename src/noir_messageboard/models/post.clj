(ns noir-messageboard.models.post
  (:require [noir.session :as session]
            [noir.validation :as vali]
            [noir-messageboard.utils.db :as db]
            [noir-messageboard.models.user :as users]))

(defn valid? [{:keys [title body]}]
  (vali/rule (vali/has-value? title)
             [:title "Please provide a title."])
  (vali/rule (vali/has-value? body)
             [:body "Please enter a message."])
  (not (vali/errors? :title :body)))

(defn owned-by-user?
  "Verify the item is owned by the logged in user."
  [postid]
  (< 0
     (:count
      (first
       (db/fetch-results
        ["SELECT COUNT(*)
          FROM posts
          WHERE id = ? AND ownerid = ?"
         (Integer/parseInt postid)
         (users/current-user-id)])))))

(defn get-list []
  (db/fetch-results
   ["SELECT p.*, users.username, users.email
     FROM posts p
     INNER JOIN users on users.id = p.ownerid
     ORDER BY p.createdat DESC"]))

(defn get-item [id]
  (first
   (db/fetch-results
    ["SELECT p.*, users.username, users.email
      FROM posts p
      INNER JOIN users ON users.id = p.ownerid
      WHERE p.id = ?"
     (Integer/parseInt id)])))

(defn add! [{:keys [title body] :as post}]
  (let [p {:ownerid (users/current-user-id)
           :title title
           :body body}]
    (when (valid? post)
      (db/insert! :posts p)
      post)))

(defn edit! [post]
  (when (and (valid? post)
             (owned-by-user? (:id post)))
    (db/update! :posts
                (Integer/parseInt (:id post))
                (dissoc post :id))
    post))

(defn delete! [id]
  (when (owned-by-user? id)
    (db/delete! :posts (Integer/parseInt id))))

(defn search [term]
  (db/fetch-results
   ["SELECT *
     FROM posts
     WHERE title LIKE ?
     OR body LIKE ?"
    (str "%" term "%")
    (str "%" term "%")]))