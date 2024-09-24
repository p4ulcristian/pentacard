(ns backend.utils
  (:require [backend.env :as env]))


(defn js->>clj [hash-map]
  (js->clj hash-map :keywordize-keys true))

(def developer-mode? 
  (let [user env/DEVELOPER?
        is-developer? (not= user "true")]
    is-developer?)) 