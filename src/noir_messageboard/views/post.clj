(ns noir-messageboard.views.post
  (:use [noir.core :only [defpage defpartial render pre-route]]
        [hiccup.core :only [escape-html]]
        [hiccup.page-helpers :only [link-to]]
        [hiccup.form-helpers :only [form-to submit-button]])
  (:require [noir-messageboard.models.post :as posts]
            [noir-messageboard.models.user :as users]
            [noir-messageboard.models.comment :as comments]
            [noir-messageboard.utils.fields :as fields]
            [noir-messageboard.utils.utils :as utils]
            [noir-messageboard.views.common :as common]
            [noir.response :as resp]
            [noir.session :as session]
            [noir.validation :as vali]))

;; Templates

(defpartial post-fields [{:keys [id title body]}]
  (fields/validated-fields
   [{:name "id" :type "hidden" :value id}
    {:name "title" :label "Title:" :value title :cssclass "xxlarge"}
    {:name "body" :label "Post:" :type "textarea" :value body :cssclass "xxlarge"}]))

(defpartial comment-fields [{:keys [id body]}]
  (fields/validated-fields
   [{:name "id" :type "hidden" :value id}
    {:name "body" :label "Add a comment:" :type "textarea" :value body :cssclass "xxlarge"}]))

(defpartial render-post [{:keys [id title createdat username commentcount]}]
  [:li
   [:div.headitem (link-to (str "/posts/" id) title)]
   [:div.byline commentcount " comments."
    " Posted " (utils/human-date createdat)
    " by " (link-to (str "/users/" username) username)]])

(defpartial render-posts [items]
  [:ol (map render-post items)])

(defpartial render-comment [comment]
  (let [username (:username comment)
        createdat (:createdat comment)]
    [:div
     [:div.byline (link-to (str "/users/" username) username)
      " replied " (utils/human-date createdat) ":"]
     [:blockquote
     [:div.comment (utils/markdownify (:body comment))]]]))

(defpartial render-comments [comments]
  [:div (map render-comment comments)])

(defpartial sticky-link [{:keys [id stickied]}]
  (if stickied
    [:a#togglesticky {:href "#" :onclick (str "togglesticky(" id "); return false;")} " [Unsticky]"]
    [:a#togglesticky {:href "#" :onclick (str "togglesticky(" id "); return false;")} " [Make Sticky]"]))
  
(defpartial post-footer [{:keys [id ownerid stickied] :as post}]
  (let [userid (:id (session/get :user))]
    [:div
     (when (or (= ownerid userid)
               (users/is-admin? userid))
       [:span
        [:span (link-to (str "/posts/edit/" id) "[Edit]")]
        " "
        [:span (link-to (str "/posts/delete/" id) "[Delete]")]
        (when (users/is-admin? userid)
          (sticky-link post))])
     [:div (link-to "/" "Return to list")]]))

(defpartial comment-block [postid comments]
  (let [username (:username (session/get :user))]
    [:div.commentblock
     [:hr]
     [:h3 (count comments) " replies"]
     [:div.comments
      (render-comments comments)
      (if-not (nil? username)
        [:div.form-stacked
         (form-to [:post (str "/posts/" postid "/addcomment")]
                  (comment-fields {})
                  (submit-button "Comment!"))]
        [:span "Log in to post a comment"])]]))

;; Pre-routing to ensure people don't go where they shouldn't

(pre-route "/posts/add" []
           (when-not (users/logged-in?)
             (resp/redirect "/login")))

(pre-route "/posts/edit/:id" {:keys [params]}
           (let [userid (:id (session/get :user))]
                 (when-not (or (posts/owned-by-user? (:id params))
                               (users/is-admin? userid))
                   (resp/redirect (str "/posts/" (:id params))))))

(pre-route "/posts/delete/:id" {:keys [params]}
           (let [userid (:id (session/get :user))]
                 (when-not (or (posts/owned-by-user? (:id params))
                               (users/is-admin? userid))
                   (resp/redirect (str "/posts/" (:id params))))))


;; Post pages

(defpage "/posts" []
  (common/layout
   "Home"
   [:h3 "Announcements"]
   (render-posts (posts/get-stickied-list))
   [:h3 "Posts"]
   (render-posts (posts/get-list))
   (link-to "/posts/add" "Add a post!")))

(defpage "/posts/add" {:as post}
  (common/layout
   "Add a post"
   [:div.form-stacked
    (form-to [:post "/posts/add"]
             (post-fields post)
             [:br]
             [:span
              [:strong (link-to
                        "http://daringfireball.net/projects/markdown/syntax/"
                        "Markdown")]
              " supported here"]
             (submit-button "Add!"))]))

(defpage [:post "/posts/add"] {:as post}
  (if (posts/add! post)
    (resp/redirect "/")
    (render "/posts/add" post)))

(defpage "/posts/:id" {:keys [id]}
  (if-let [post (posts/get-item id)]
    (let [title (escape-html (:title post))
          body (utils/markdownify (:body post))
          createdat (utils/human-date (:createdat post))
          username (:username post)
          comments (posts/get-comments (:id post))]
      (common/layout
       title
       [:div body
        [:h5 "Added " createdat " by: " (link-to (str "/users/" username) username)]
        (post-footer post)
        (comment-block id comments)]))
    (common/layout
     [:h3 "Post not found"])))

(defpage "/posts/edit/:id" {:keys [id] :as editpost}
  ;; Display the passed in post if it's a valid object, otherwise, fetch from the db
  (if-let [post (if (every? editpost [:id :title :body])
                  editpost
                  (posts/get-item id))]
    (common/layout
     "Edit Post"
     [:div.form-stacked
      (form-to [:post (str "/posts/edit/" id)]
               (post-fields post)
               (submit-button "Save Changes"))])
    (resp/redirect "/posts")))
       
(defpage [:post "/posts/edit/:id"] {:keys [id] :as post}
  (if (posts/edit! post)
    (resp/redirect (str "/posts/" id))
    (render "/posts/edit/:id" post)))
    

(defpage [:post "/posts/:id/addcomment"] {:keys [id] :as comment}
  (let [c {:postid id
           :body (:body comment)
           :parentid -1}]
          (if (comments/add! c)
            (resp/redirect (str "/posts/" id))
            (render "/posts/:id" id))))


(defpage "/posts/delete/:id" {:keys [id] :as deletepost}
  (if-let [post (posts/get-item id)]
    (common/layout
     (str "Delete post: '" (:title post) "'?")
     (form-to [:post (str "/posts/delete/" id)]
              (submit-button "Delete post!")))
    (resp/redirect "/posts")))

(defpage [:post "/posts/delete/:id"] {:keys [id]}
  (posts/delete! id)
  (resp/redirect "/posts"))


(defpage [:post "/togglesticky"] {:keys [postid]}
  (let [post (posts/get-item  postid)
        userid (:id (session/get :user))]
    (when (users/is-admin? userid)
      (posts/toggle-post-sticky! post))))