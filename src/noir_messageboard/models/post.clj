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
   ["SELECT p.*, users.username, users.email,
           (SELECT COUNT(*) FROM comments
            WHERE postid = p.id) as commentcount
     FROM posts p
     INNER JOIN users on users.id = p.ownerid
     WHERE p.stickied = '0'
     ORDER BY p.createdat DESC"]))

(defn get-stickied-list []
  (db/fetch-results
   ["SELECT p.*, users.username, users.email,
           (SELECT COUNT(*) FROM comments
            WHERE postid = p.id) as commentcount
     FROM posts p
     INNER JOIN users on users.id = p.ownerid
     WHERE p.stickied = '1'
     ORDER BY p.createdat DESC"]))   

(defn get-item [id]
  (first
   (db/fetch-results
    ["SELECT p.*, users.username, users.email
      FROM posts p
      INNER JOIN users ON users.id = p.ownerid
      WHERE p.id = ?"
     (Integer/parseInt id)])))

(defn get-comments
  "Get all the comments for a post."
  [id]
  (db/fetch-results
   ["SELECT c.*, users.username, users.email
     FROM comments c
     INNER JOIN users on users.id = c.ownerid
     WHERE c.postid = ?
     ORDER BY c.createdat"
    id]))

(defn toggle-post-sticky!
  "Turn a regular post into a stickied post. Only admins should do this."
  [post]
  (let [postid (:id post)
        stickied-post {:id postid
                       :stickied (not (:stickied post))}]
  (db/update! :posts
              postid
              stickied-post)
  (str (:stickied post))))
  
(defn add! [{:keys [title body] :as post}]
  (let [p {:ownerid (users/current-user-id)
           :title title
           :stickied false
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