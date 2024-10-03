(ns frontend.pentacard.events.game
  (:require [re-frame.alpha :refer [reg-event-db reg-event-fx dispatch]]))
            

(defn get-last-card [cards]
  (last
   (sort (fn [[_ card-one] [_ card-two]] 
           (compare (:index card-one) (:index card-two)))
         cards)))

(defn filter-by-origin [cards origin]
  (filter 
   (fn [[card-id card-data]]
     (= (:origin card-data) origin))
   cards))

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
                         (assoc-in [last-card-id :index] next-index))]
     (println "Drawing card" (count drawing-deck)
              cards)
     (assoc-in db [:game :cards] new-cards))))
         