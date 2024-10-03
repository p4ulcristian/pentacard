(ns frontend.pentacard.events.animations.deal-cards
  (:require [re-frame.alpha :refer [reg-event-db]]))


(defn offset-by-index [position index]
  (let [[x y z] position
        new-x (if (= 0 (rem index 2))
                (- x 0.07)
                (+ x 0.07))
        new-y (+ y (* (quot index 2) 0.125))] 
    [new-x new-y z]))

(defn one-deal-animation [db ref player-index card-index]
  (let [players-count (-> db :players-count)
        position (get-in db [:positions :players players-count player-index])
        [x y z] (offset-by-index position card-index)]
    (println "animate: " (offset-by-index position card-index))
    (set! (-> ref .-current .-position .-x) x)
    (set! (-> ref .-current .-position .-y) y)
    (set! (-> ref .-current .-position .-z) z)))


; [a b c d] -> [[0 a] [1 b] [2 c] [3 d]]

(defn deal-animation [db cards-by-player]
  
  (let [card-objects (get-in db [:objects :cards])]
    (doseq [[card-id card-data] cards-by-player]  
      (let [ref (get-in card-objects [card-id])]
        (one-deal-animation db ref (:origin card-data) (:index card-data))))))


(reg-event-db
 :animation/deal-cards!
 ;; cards-by-player [[card-id card-id ...] [card-id card-id ...] ...]
 (fn [db [_ cards-by-player]]
   (let []
     (println "Animation " cards-by-player)
     (deal-animation db cards-by-player)
     db)))