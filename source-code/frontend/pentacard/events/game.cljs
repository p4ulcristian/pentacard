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
                         (assoc-in [last-card-id :index]  next-index))]
     (dispatch [:animation/draw-card! last-card-id])
     (assoc-in db [:game :cards] new-cards))))
         
(defn add-players-to-cards [cards player-cards-by-player]
  (let [player-cards-with-indexes
        (map-indexed (fn [i cards] [i (map 
                                       (fn [card-id]
                                         [i card-id])
                                       cards)]) 
                     player-cards-by-player)
        player-cards (reduce 
                      (fn [this-cards [player-index player-card-id]]
                        (assoc-in this-cards [player-card-id :origin] player-index))
                      cards
                      player-cards-with-indexes)
        ]
    player-cards))

(reg-event-db 
 :game/deal-cards! 
 (fn [db [_]]
   (let [cards (-> db :game :cards)
         drawing-deck (filter-by-origin cards :drawing-deck)
         shuffled-deck (shuffle drawing-deck)
         players-count (-> db :players-count)
         cards-per-player 4
         total-cards (* players-count cards-per-player)
         player-cards  (take total-cards (map first shuffled-deck))
         player-cards-by-player (partition cards-per-player player-cards)
         new-cards (add-players-to-cards cards player-cards-by-player)]
    
     (assoc-in db [:game :cards] new-cards))))