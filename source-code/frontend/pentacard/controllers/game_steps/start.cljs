(ns frontend.pentacard.controllers.game-steps.start
  (:require [frontend.pentacard.utils.data :as data]))




(defn deal-cards [cards-to-deal rest-of-cards players-count] 
  (merge rest-of-cards 
         (data/re-index-with-same-order
          (reduce merge
                  (map (fn [[card-id card-data]]
                         {card-id (assoc card-data :origin :player-1)})
                       cards-to-deal)))))
  

(defn start [db]
  (let [cards (:cards db)
        players-count  (:players-count db)
        shuffled-cards (shuffle cards)
        cards-to-deal  (data/vector-pairs->map (subvec shuffled-cards 0 (* 4 players-count)))
        rest-of-cards  (data/vector-pairs->map (subvec shuffled-cards (* 4 players-count)))] 
    (assoc db :cards (deal-cards cards-to-deal rest-of-cards players-count))))