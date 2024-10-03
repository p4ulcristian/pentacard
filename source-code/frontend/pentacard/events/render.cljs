(ns frontend.pentacard.events.render
  (:require [re-frame.alpha :refer [subscribe reg-event-db]]
            [re-frame.db :refer [app-db]]))


(def debug?  true)

(defn render-function []
  (let [functions (get-in @app-db [:render-functions])] 
    (when debug? (println "Running renders: " (count functions) (keys functions)))
    (doseq [[function-id function] functions]
      (try (function)
           (catch js/Error e (println "Error in render function" function-id e))))
    (.requestAnimationFrame js/window render-function)))

(.requestAnimationFrame js/window render-function)

(reg-event-db 
 :render/add-callback 
 (fn [db [_ callback-id callback]] 
   (when debug? (println "added callback" callback-id callback))
   (assoc-in db [:render-functions callback-id] callback)))

(reg-event-db
 :render/remove-callback
 (fn [db [_ callback-id]]
    (when debug? (println "removed callback" callback-id))
   (let [render-functions (-> db :render-functions)]
     (assoc-in db [:render-functions] (dissoc render-functions callback-id)))))