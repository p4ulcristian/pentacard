(ns frontend.pentacard.controllers.game
  (:require [re-frame.core :refer [reg-event-db dispatch]]
            ["@react-spring/three" :refer [SpringValue]]))


(defn filter-origin [cards filter-value]
  (filter
   (fn [[card-id {:keys [origin]}]]
     (= filter-value origin))
   cards))

(reg-event-db
 :cards/draw!
 (fn [db [_ id]] 
   (let [cards         (-> db :cards)
         discard-deck  (filter-origin cards :discard-deck)
         next-index    (count discard-deck)]
     (-> db 
         (assoc-in [:cards id :origin]  :discard-deck)
         (assoc-in [:cards id :index]   next-index)))))



(reg-event-db
 :game/start!
 (fn [db [_]]
   (let [cards (-> db :cards)
         drawing-deck (filter 
                       (fn [[card-id {:keys [origin]}]]
                         (= :drawing-deck origin))
                       cards)] 
     (when-not 
      (empty? drawing-deck)
       (let [last-card-id (first (last drawing-deck))]
         (dispatch [:cards/draw! last-card-id]))))))