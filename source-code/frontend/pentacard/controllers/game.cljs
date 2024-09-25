(ns frontend.pentacard.controllers.game
  (:require [re-frame.core :refer [reg-event-db dispatch]]
            ["@react-spring/three" :refer [SpringValue]]))


(defn callback [^js ref ^js spring-value]
  (let [has-animated? (.-hasAnimated spring-value)
        is-animating? (.-isAnimating spring-value)
        finished?  (and has-animated? (not is-animating?))]
    (println "heey:")
    (when-not finished? 
      (set! (-> ^js ref .-current .-position .-x) (.get spring-value))
      (.requestAnimationFrame js/window #(callback ref spring-value)))))

(defn animation [ref]
  (let [spring-value (new SpringValue 0 #js {:to 0.3
                                             :delay 500
                                             :duration 1000
                                             ;:loop true
                                             :config #js {:mass 5}})]
    
    (.requestAnimationFrame js/window #(callback ref spring-value))))

(reg-event-db
 :game/start!
 (fn [db [_]]
   (let [ref (-> db :animated-example :ref)]
     (println "heh: " ref)
     (animation ref)
     (assoc-in db [:animated-example :animated?] (not (get-in db  [:animated-example :animated?]))))))