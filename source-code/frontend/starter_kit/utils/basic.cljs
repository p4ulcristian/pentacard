(ns frontend.starter-kit.utils.basic
  (:require [cljs.pprint :as pprint]))


(defn generate-keyword []
  (keyword (str (random-uuid))))

(defn pretty-print [& arg]
  (pprint/pprint arg))

(defn pretty-print-string [arg]
  (with-out-str
    (pretty-print arg)))



(defn js->>clj [hash-map]
  (js->clj hash-map :keywordize-keys true))