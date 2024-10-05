(ns frontend.pentacard.events.render
  (:require [re-frame.alpha :refer [dispatch subscribe reg-event-db]]
            [re-frame.db :refer [app-db]]
            [frontend.pentacard.events.animations.utils :as utils]
            ["@react-spring/three" :refer [SpringValue]]))


(def debug?  false)

(defn animation-finished? [^js spring-value]
  (let [has-animated? (.-hasAnimated spring-value)
        is-animating? (.-isAnimating spring-value)
        finished?     (and has-animated? (not is-animating?))]
    finished?))

(defn animation-function [{:keys [spring-value attribute ref]}]
  (let []
    (println "mi tortenik" attribute)
    (aset (-> ref .-current .-position) 
          attribute
          (.get spring-value))
    ))


(defn render-function []
  (let [functions (get-in @app-db [:render-functions])] 
    (when debug? (println "Running renders: " (count functions) (keys functions)))
    (doseq [[function-id {:keys [callback-id spring-value ref attribute]}] functions]
      (if (animation-finished? spring-value) 
        (dispatch [:render/remove-callback callback-id])
        (try (animation-function {:spring-value spring-value
                                  :attribute attribute
                                  :ref ref}) 
             (catch js/Error e (println "Error in render function" function-id e)))
        ))
    (.requestAnimationFrame js/window render-function)))

(.requestAnimationFrame js/window render-function)

(reg-event-db 
 :render/add-animation 
 (fn [db [_ {:keys [ from to attribute ref]}]]
   (let [callback-id (utils/id)
         spring-value (new SpringValue from
                           #js {:to to
                                :config #js {:mass 2}})] 
     (when debug? (println "added callback" callback-id ))
     (assoc-in db [:render-functions callback-id] {:callback-id  callback-id
                                                   :spring-value spring-value
                                                   :attribute attribute
                                                   :ref ref}))))

(reg-event-db
 :render/remove-callback
 (fn [db [_ callback-id]]
    (when debug? (println "removed callback" callback-id))
   (let [render-functions (-> db :render-functions)]
     (assoc-in db [:render-functions] (dissoc render-functions callback-id)))))