(ns frontend.starter-kit.subscriptions.db
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :db/get
 (fn [db [_ path]]
   (get-in db path)))

(reg-sub
 :db/get-multiple-paths-and-merge
 (fn [db [_ paths]]
   (let [path->value   (fn [path] (get-in db path))
         reduce-fn     (fn [result path]
                         (let [this-value   (path->value path)
                               this-key     path]
                           (assoc result this-key this-value)))]

     (reduce reduce-fn {} paths))))