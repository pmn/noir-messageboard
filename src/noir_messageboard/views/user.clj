(ns noir-messageboard.views.user
  (:use [noir.core :only [defpage defpartial render pre-route]]
        [hiccup.form-helpers :only [form-to submit-button]]
        [hiccup.page-helpers :only [link-to]])
  (:require [noir-messageboard.models.user :as users]
            [noir-messageboard.utils.fields :as fields]
            [noir-messageboard.utils.utils :as utils]
            [noir-messageboard.views.common :as common]
            [noir.session :as session]
            [noir.validation :as vali]
            [noir.response :as resp]))

;; Pre-routes

(pre-route "/profile" []
           (when-not (users/logged-in?)
             (resp/redirect "/login")))

(pre-route "/changepw" []
           (when-not (users/logged-in?)
             (resp/redirect "/login")))

;; Partial templates

(defpartial registration-fields [{:keys [username password confirmpass]}]
  (fields/validated-fields
   [{:name "username" :label "Username:" :value username}
    {:name "password" :label "Password:" :type "password"}
    {:name "confirmpass" :label "Confirm:" :type "password"}]))

(defpartial login-fields [{:keys [username password]}]
  (fields/validated-fields
   [{:name "username" :label "Username:" :value username}
    {:name "password" :label "Password:" :type "password"}]))

(defpartial profile-fields [{:keys [email essay]}]
  (fields/validated-fields
   [{:name "email" :label "Email Address:" :value email :cssclass "xlarge"}
    {:name "essay" :label "About you (markdown supported):" :value essay
     :type "textarea" :cssclass "xlarge"}]))

(defpartial changepw-fields [{:keys [password confirmpass]}]
  (fields/validated-fields
   [{:name "password" :label "New Password:" :type "password"}
    {:name "confirmpass" :label "Confirm:" :type "password"}]))

(defpartial render-user-post [{:keys [id title createdat]}]
  [:li (link-to (str "/posts/" id) title)
   [:div.byline "Posted " (utils/human-date createdat)]])

(defpartial render-user-posts [items]
  [:ol (map render-user-post items)])

;; Pages

(defpage "/register" {:as user}
  (common/layout
   "Register"
   [:div.form-stacked
    (form-to [:post "/register"]
             (registration-fields user)
             (submit-button "Register!"))]))

(defpage [:post "/register"] {:as user}
  (if (users/add! user)
    (do (users/login user)
        (resp/redirect "/"))
    (render "/register" user)))

(defpage "/login" {:as user}
  (common/layout
   "Log in"
   [:div.form-stacked
    (form-to [:post "/login"]
             (login-fields user)
             (submit-button "Log in"))]))

(defpage [:post "/login"] {:as user}
  (if (users/login user)
    (resp/redirect "/")
    (render "/login" user)))

(defpage "/logout" []
  (users/logout)
  (resp/redirect "/"))

(defpage "/profile" {:as profile}
  (let [userprofile (if (every? profile [:email :essay])
                      profile
                      (users/get-by-username (:username (session/get :user))))]
    (common/layout
     "Edit your details"
     [:div (link-to "/changepw" "Change your password")]
     [:div.form-stacked
      (form-to [:post "/profile"]
               (profile-fields userprofile)
               (submit-button "Save"))])))

(defpage [:post "/profile"] {:as profile}
  (if (users/save-profile! profile)
    (resp/redirect "/")
    (render "/profile" profile)))

(defpage "/changepw" {:as changepw}
  (common/layout
   "Change your password"
   [:div.form-stacked
    (form-to [:post "/changepw"]
             (changepw-fields changepw)
             (submit-button "Reset Password"))]))

(defpage [:post "/changepw"] {:as changepw}
  (if (users/change-password! changepw)
    (resp/redirect "/")
    (render "/changepw" changepw)))

(defpage "/users/:username" {:keys [username]}
  (if-let [user (users/get-by-username username)]
    (common/layout
     (str "About " username)
     [:div.about (utils/markdownify (:essay user))]
     [:h3 username "'s posts"]
     (render-user-posts (users/get-posts (:id user))))
    (common/layout
     "User not found"
     [:h3 "User not found"])))