(ns frontend.pentacard.controllers.state
  (:require [re-frame.core :refer [reg-event-db dispatch]]))

(def suits ["Hearts" "Diamonds" "Clubs" "Spades"])
(def suits-emojis {"Hearts"   "\u2665"
                   "Diamonds" "\u2666"
                   "Clubs"    "\u2663"
                   "Spades"   "\u2660"})
(def ranks ["Ace" "2" "3" "4" "5" "6" "7" "8" "9" "10" "Jack" "Queen" "King"])

(def deck
  (for [suit suits
        rank ranks]
    {:rank rank
     :suit suit
     :suit-emoji (get suits-emojis suit)
     :state :drawing-deck}))


(defn pentagon-points
  "Calculate the coordinates of the vertices of a regular pentagon,
   relative to the center of the pentagon.
   side-length: the length of each side.
   Returns a list of [x y] coordinates."
  [side-length]
  (let [n 5
        angle-increment  (/ (* 2 Math/PI) n)
        radius           (/ side-length (* 2 (Math/sin (/ Math/PI n))))]
    (for [i (range n)]
      (let [angle (- (* i angle-increment) (/ Math/PI 2)) ; rotate to align first point vertically
            x (* radius (Math/cos angle))
            z (* radius (Math/sin angle))]
        [x 0 z]))))

(def state 
  {:cards deck
   :boards {:pentagon {:points (pentagon-points 0.5)}}})

(reg-event-db 
 :state/setup! 
 (fn [db []]
   state))
