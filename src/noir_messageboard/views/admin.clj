(ns noir-messageboard.views.admin
  (:use [noir.core :only [defpage defpartial render pre-route]]
        [hiccup.page-helpers :only [link-to]]
        [hiccup.form-helpers :only [form-to submit-button]]
        [noir-messageboard.views.user :only [changepw-fields]])
  (:require [noir-messageboard.models.post :as posts]
            [noir-messageboard.models.user :as users]
            [noir-messageboard.models.comment :as comments]
            [noir-messageboard.utils.fields :as fields]
            [noir-messageboard.views.common :as common]
            [noir.response :as resp]
            [noir.session :as session]))

;; Pre-routing to make sure people don't go where they shouldn't

(pre-route "/users" []
           (when-not (users/is-admin? (:id (session/get :user)))
             (resp/redirect "/")))

(pre-route "/users/:id/changepass" []
           (when-not (users/is-admin? (:id (session/get :user)))
             (resp/redirect "/")))

(pre-route "/users/:id/delete" []
           (when-not (users/is-admin? (:id (session/get :user)))
             (resp/redirect "/")))
           
(defpartial render-user [user]
  [:tr
   [:td (link-to (str "/users/" (:username user)) (:username user))]
   [:td (:email user)]
   [:td (:createdat user)]
   [:td (:postcount user)]
   [:td (:commentcount user)]
   [:td
    (link-to (str "/users/" (:id user) "/changepass") "[Reset Pass]")
    " "
    (link-to (str "/users/" (:id user) "/delete") "[Delete]")
    ]])

(defpartial render-user-list [users]
  [:table
   [:tr
    [:th "Username"]
    [:th "Email"]
    [:th "Created At"]
    [:th "# Posts"]
    [:th "# Comments"]
    [:th]]
   (map render-user users)])

(defpage "/users" []
  (common/layout
   "User list"
   (render-user-list (users/get-list))))

(defpage "/users/:id/delete" {:keys [id]}
  (let [user (users/get-by-id id)]
    (common/layout
     (str "Delete user - " (:username user))
     (form-to [:post (str "/users/" id "/delete")]
              [:div "Are you sure you want to delete this user?"]
              [:div "This will remove all of their posts and comments."]
              (submit-button "Delete user")))))

(defpage [:post "/users/:id/delete"] {:keys [id]}
  (let [user (users/get-by-id id)]
    (users/delete! id)
    (session/flash-put! (str "Deleted user: " (:username user)))
    (resp/redirect "/users")))


(defpage "/users/:id/changepass" {:keys [id] :as changepw}
  (let [user (users/get-by-id id)]
    (common/layout
     (str "Change password for " (:username user))
     [:div.form-stacked
     (form-to [:post (str "/users/" id "/changepass")]
              [:input {:type "hidden" :name "uid" :value id}]
              (changepw-fields changepw)
              (submit-button "Reset Password"))])))

(defpage [:post "/users/:id/changepass"] {:as changepw}
  (if (users/admin-change-password! changepw)
    (resp/redirect "/")
    (render (str "/users/:id/changepass") changepw)))
