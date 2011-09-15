(ns noir-messageboard.views.home
  (:use [noir.core :only [defpage]])
  (:require [noir.response :as resp]))


(defpage "/" []
  (resp/redirect "/posts"))