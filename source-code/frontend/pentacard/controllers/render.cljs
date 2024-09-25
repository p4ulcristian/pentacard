(ns frontend.pentacard.controllers.render
  (:require [re-frame.alpha :refer [subscribe reg-event-db]]
            [re-frame.db :refer [app-db]]))




(defn render-function []
  (let [functions (get-in @app-db [:render-functions])] 
    (println "Running renders: " (count functions) (keys functions))
    (doseq [[function-id function] functions]
      (function))
    (.requestAnimationFrame js/window render-function)))

(.requestAnimationFrame js/window render-function)

(reg-event-db 
 :render/add-callback
 (fn [db [_ callback-id callback]] 
   (println "added callback" callback-id callback)
   (assoc-in db [:render-functions callback-id] callback)))

(reg-event-db
 :render/remove-callback
 (fn [db [_ callback-id]]
    (println "removed callback" callback-id)
   (let [render-functions (-> db :render-functions)]
     (assoc-in db [:render-functions] (dissoc render-functions callback-id)))))