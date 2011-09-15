(ns noir-messageboard.utils.fields
  (:use [noir.core :only [defpartial]])
  (:require [hiccup.form-helpers :as form]
            [noir.validation :as vali]))

(defpartial error-item [[first-error]]
  [:div.error first-error])

(defpartial validated-field [{:keys [name type label value cssclass]}]
  (when-not (= type "hidden") (form/label name label))
  (cond
   (= type "text") [:input {:type "text" :id name :name name :value value :class cssclass}]
   (= type "password") [:input {:type "password" :id name :name name :class cssclass}]
   (= type "textarea") [:textarea {:id name :name name :class cssclass :rows 10} value]
   (= type "hidden") [:input {:type "hidden" :id name :value value}]
   :else [:input {:type "text" :id name :name name :value value :class cssclass}])
  (vali/on-error (keyword name) error-item))

(defpartial validated-fields [fields]
  (map validated-field fields))