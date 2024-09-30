(ns frontend.pentacard.controllers.game-steps.start
  (:require [frontend.pentacard.utils.data :as data]
            [re-frame.alpha :refer [dispatch]]))




(defn deal-cards [cards-to-deal players-count] 
  (reduce merge
          (map-indexed
           (fn [index [card-id card-data]]
             {card-id (assoc card-data 
                             :index  (quot index players-count)
                             :origin (keyword (str "player-" (rem index players-count))))})
           cards-to-deal)))

(def timeout 300)




(defn add-cards-one-by-one [cards]
  (doseq [[index [card-id card-data]] (map-indexed vector cards)]
    (.setTimeout js/window #(dispatch [:db/set [:cards card-id] card-data]) 
                 (* index timeout))))

(defn start [db]
  (let [cards (:cards db)
        players-count  (:players-count db)
        shuffled-cards (shuffle cards)
        cards-to-deal  (data/vector-pairs->map (subvec shuffled-cards 0 (* 4 players-count)))
        rest-of-cards  (data/vector-pairs->map (subvec shuffled-cards (* 4 players-count)))
        cards-after-deal (deal-cards cards-to-deal players-count)] 
    (add-cards-one-by-one cards-after-deal)
    db))