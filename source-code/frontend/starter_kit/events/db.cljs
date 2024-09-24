(ns frontend.starter-kit.events.db
  (:require [my-re-frame :refer [reg-event-db]]))





(reg-event-db
 :db/set
 (fn [db [_ path value]]
   (assoc-in db path value)))


(defn get-parent-path [path]
  (vec (drop-last 1 path)))


(reg-event-db
 :db/unset
 (fn [db [_ path]]
   (let [parent-path (get-parent-path path)
         parent (get-in db parent-path)
         child-key (last path)]
     (assoc-in db parent-path (dissoc parent child-key)))))



(reg-event-db
 :db/merge
 (fn [db [_ path value-to-merge]]
   (let [value       (get-in db path)
         new-value   (merge value value-to-merge)]
     (assoc-in db path new-value))))