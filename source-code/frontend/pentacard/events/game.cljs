(ns frontend.pentacard.events.game
  (:require [re-frame.alpha :refer [reg-event-db reg-event-fx dispatch]]))
            


(defn get-last-index [cards]
  (dec (count cards)))

(defn get-last-card [cards]
  (let [last-index (get-last-index cards)]  
    (first (filter (fn [[_ card]] 
                     (= last-index (:index card))) cards))))


(defn update-card-index [card discard-deck]
  (let [new-index (count discard-deck)]
    (assoc card :index new-index)))

(reg-event-db
 :game/draw!
 (fn [db [_]]
   (let [drawing-deck (get-in db [:drawing-deck :cards])
         discard-deck (get-in db [:discard-deck :cards])
         last-card (get-last-card drawing-deck) 
         [last-card-key last-card-value] last-card
         new-last-card    (update-card-index last-card-value discard-deck)
         new-drawing-deck (dissoc drawing-deck last-card-key)
         new-discard-deck (assoc discard-deck last-card-key new-last-card)]
     (-> db
         (assoc-in [:drawing-deck :cards] new-drawing-deck)
         (assoc-in [:discard-deck :cards] new-discard-deck)))))