(ns frontend.pentacard.events.game
  (:require [re-frame.alpha :refer [reg-event-db reg-event-fx dispatch]]
            [frontend.pentacard.events.game.deal-cards]
            [frontend.pentacard.events.utils :refer [filter-by-origin]]))
            

(defn get-last-card [cards]
  (last
   (sort (fn [[_ card-one] [_ card-two]] 
           (compare (:index card-one) (:index card-two)))
         cards)))


(reg-event-db
 :game/draw!
 (fn [db [_]]
   (let [cards (-> db :game :cards)
         drawing-deck  (filter-by-origin cards :drawing-deck) 
         discard-deck  (filter-by-origin cards :discard-deck)
         next-index    (count discard-deck)
         last-card     (get-last-card drawing-deck)
         [last-card-id _] last-card
         new-cards      (-> 
                         cards
                         (assoc-in [last-card-id :origin] :discard-deck)
                         (assoc-in [last-card-id :index]  next-index))]
     (dispatch [:animation/draw-card! last-card-id])
     (assoc-in db [:game :cards] new-cards))))

