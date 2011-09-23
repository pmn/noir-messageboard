(ns noir-messageboard.utils.utils
  (:use [hiccup.core :only [escape-html]])
  (:require [clj-time.core :as ctime]
            [clj-time.coerce :as tcoerce]
            [clj-time.format :as tform]
            [noir.validation :as vali])
  (:import [com.petebevin.markdown MarkdownProcessor]))

;; Markdown Processing

(def mdp (com.petebevin.markdown.MarkdownProcessor.))

(defn markdownify [text]
  (.markdown mdp (escape-html text)))

;; humanize dates

(defn describe-time-elapsed
  "Describe the amount of time that has passed (in minutes) in a conversational way"
  [minutes]
  (let [[hours days months years]
        (for [amt [60.0 1440.0 43829.0639 525948.766]]
          (Math/round (/ minutes amt)))]
    (cond
     (= minutes 0) "just now"
     (< minutes 2) "a minute ago"
     (< minutes 60) (str minutes " minutes ago")
     (= hours 1) "an hour ago"
     (< hours 24) (str hours " hours ago")
     (= days 1) "yesterday"
     (< days 7) (str days " days ago")
     (= 1 (Math/round (/ days 7.0))) "1 week ago"
     (< days 31) (str (Math/round (/ days 7.0)) " weeks ago")
     (= months 1) "a month ago"
     (< months 12) (str months " months ago")
     (= years 1) "a year ago"
     :else (str years " years ago"))))

(defn human-date
  "Convert a timestamp to a human readable date date, like '3 minutes ago' or '2 weeks ago'."
  [t]
  (let [minutes-elapsed (ctime/in-minutes
                         (ctime/interval
                          (tcoerce/from-long (.getTime t)) (ctime/now)))
        datetime (tform/unparse (tform/formatters :rfc822)
                                (tcoerce/from-long (.getTime t)))]
    (describe-time-elapsed minutes-elapsed)))
  
;; Encryption helper

(defmacro with-crypted
  "Wrap password crypting so that the work factor only needs to
   get updated in a single place."
  [password]
  `(crypt/encrypt (crypt/gen-salt 12) ~password))