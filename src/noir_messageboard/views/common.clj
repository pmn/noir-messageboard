(ns noir-messageboard.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [html5 include-css link-to]])
  (:require [noir.session :as session]))

(defpartial userbar []
  (let [username (:username (session/get :user))
        profilelink (link-to "/profile" username)
        logoutlink (link-to "/logout" "log out")
        loginlink (link-to "/login" "Log in")
        registerlink (link-to "/register" "Register")]
    (if-not (nil? username)
      [:span profilelink " (" logoutlink ")"]
      [:span loginlink " or " registerlink])))

(defpartial layout [title & content]
            (html5
              [:head
               [:title title " | noir-messageboard"]
               (include-css "/css/reset.css")
               (include-css "/css/site.css")
               (include-css "/css/extra.css")]
              [:body.container
               [:div.row
                [:h1.span3.columns (link-to "/" "Messageboard!")]
                [:div.span4.columns.offset9 (userbar)]]
               [:div#wrapper
                [:h2 title]
                [:hr]
                content]
               [:hr]]))
