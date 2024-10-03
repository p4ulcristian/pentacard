(ns frontend.pentacard.events.animations
 (:require [re-frame.alpha :refer [reg-event-db dispatch]]
           ["@react-spring/three" :refer [SpringValue]]
           [re-frame.db :as db]))

(defn animation-finished? [^js spring-value]
  (let [has-animated? (.-hasAnimated spring-value)
        is-animating? (.-isAnimating spring-value)
        finished?     (and has-animated? (not is-animating?))]
    finished?))

(defn callback-x [^js ref ^js spring-value callback-id] 
  (if-not (animation-finished? spring-value)
    (set! (-> ^js ref .-current .-position .-x) (.get spring-value))
    (do (dispatch [:render/remove-callback callback-id]) 
        (println "Animation finished"))))


(defn animate-position! [ref]
 (let [x-callback-id (str (random-uuid)) 
       x-spring (new SpringValue -0.1
                     #js {:to 0.1
       
                          :config #js {:mass 2}})]
   (println "hello" x-callback-id)
   (dispatch [:render/add-callback 
              x-callback-id
              #(callback-x ref x-spring x-callback-id)])))

(reg-event-db
 :animation/draw-card!
 (fn [db [_ object-key]]
   (let [ref (get-in db [:objects :cards object-key])]
     
     (animate-position! ref) 
     db)))
   