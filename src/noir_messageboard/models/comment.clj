(ns noir-messageboard.models.comment
  (:require [noir.session :as session]
            [noir.validation :as vali]
            [noir-messageboard.utils.db :as db]
            [noir-messageboard.models.user :as users]))


(defn valid? [body]
  (vali/rule (vali/has-value? body)
             [:body "Please enter a comment."])
  (not (vali/errors? :body)))

(defn owned-by-user?
  "Verify the comment is owned by the logged-in user."
  [commentid]
  (< 0
     (:count
      (first
       (db/fetch-results
        ["SELECT COUNT(*)
          FROM comments
          WHERE id = ? AND ownerid = ?"
         (Integer/parseInt commentid)
         (users/current-user-id)])))))

(defn add! [{:keys [body postid parentid] :as comment}]
  (let [c {:ownerid (users/current-user-id)
           :postid (Integer/parseInt postid)
           :parentid parentid
           :body body}]
    (when (valid? comment)
      (db/insert! :comments c)
      comment)))

(defn edit! [comment]
  (when (and (valid? comment)
             (owned-by-user? (:id comment)))
    (db/update! :comments
                (Integer/parseInt (:id comment))
                (dissoc comment :id))
    comment))

(defn delete! [id]
  (when (owned-by-user? id)
    (db/delete! :posts (Integer/parseInt id))))