(ns frontend.pentacard.events.game.deal-cards
  (:require  [frontend.pentacard.events.utils :refer [filter-by-origin]]
             [re-frame.alpha :refer [reg-event-db reg-event-fx dispatch]]))


(defn get-player-cards [db cards]
  (let [cards-per-player 4
        players-count (-> db :players-count)
        total-cards (* players-count cards-per-player) 
        player-cards  (take total-cards cards)
         ;Partition the cards by player [[player1-cards] [player2-cards] ...]
        new-cards  (reduce merge 
                           (map-indexed (fn [index [card-id card-data]]
                                          {card-id 
                                           (assoc card-data
                                                  :origin (quot index cards-per-player) 
                                                  :index  (rem index cards-per-player))})
                                        player-cards))]
    new-cards))  

(reg-event-db
 :game/deal-cards!
 (fn [db [_]]
   (let [cards (-> db :game :cards)
         drawing-deck (filter-by-origin cards :drawing-deck)
         shuffled-deck (shuffle drawing-deck) 
         player-cards (get-player-cards db shuffled-deck)
         new-cards (merge cards player-cards)]
     (dispatch [:animation/deal-cards! player-cards])
     (assoc-in db [:game :cards] new-cards))))