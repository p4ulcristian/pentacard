(ns frontend.pentacard.controllers.game-steps.start
  (:require [frontend.pentacard.utils.data :as data]))



(defn deal-cards [cards-to-deal rest-of-cards players-count] 
  (merge rest-of-cards 
         (reduce merge
                 (map-indexed
                  (fn [index [card-id card-data]]
                    {card-id (assoc card-data 
                                    :index  (quot index players-count)
                                    :origin (keyword (str "player-" (rem index players-count))))})
                  cards-to-deal))))
  

(defn start [db]
  (let [cards (:cards db)
        players-count  (:players-count db)
        shuffled-cards (shuffle cards)
        cards-to-deal  (data/vector-pairs->map (subvec shuffled-cards 0 (* 4 players-count)))
        rest-of-cards  (data/vector-pairs->map (subvec shuffled-cards (* 4 players-count)))] 
    (assoc db :cards (deal-cards cards-to-deal rest-of-cards players-count))))