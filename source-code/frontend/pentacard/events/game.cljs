(ns frontend.pentacard.events.game
  (:require [re-frame.alpha :refer [reg-event-db reg-event-fx dispatch]]))
            

(defn get-last-card [cards]
  (sort (fn [[_ card-one] [_ card-two]] 
          (compare (:index card-one) (:index card-two)))
        cards))

(defn filter-by-origin [cards origin]
  (filter 
   (fn [card]
     (= (:origin card) origin))
   cards))

(reg-event-db
 :game/draw!
 (fn [db [_]]
   (let [cards (-> db :game :cards)
         drawing-deck  (filter-by-origin cards :drawing-deck) 
         last-card     (get-last-card drawing-deck)
         [last-card-id _] last-card
         new-cards      (assoc-in cards [last-card-id :origin] :discard-deck)]
     (assoc db :cards new-cards))))
         