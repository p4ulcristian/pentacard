(ns frontend.pentacard.controllers.state
  (:require [re-frame.alpha :refer [reg-flow reg-event-db dispatch]]))

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
     :origin :drawing-deck}))


(def deck-with-keys 
  (reduce merge 
          (map-indexed
           (fn [index card] {(keyword (str (random-uuid)))
                             (assoc card :index index)})
           deck)))

;; (reg-flow
;;  {:id     :all-cards
;;   :inputs {:cards [:cards]}
;;   :output (fn [{:keys [cards]}]  (reduce merge (map second cards)))
;;   :path   [:cards :all-cards]})


(defn pentagon-points
  "Calculate the coordinates of the vertices of a regular pentagon,
   relative to the center of the pentagon.
   side-length: the length of each side.
   Returns a list of [x y] coordinates."
  [side-length]
  (let [n 5
        angle-increment  (/ (* 2 Math/PI) n)
        radius           (/ side-length (* 2 (Math/sin (/ Math/PI n))))]
    (vec (for [i (range n)]
           (let [angle (- (* i angle-increment) (/ Math/PI 2)) ; rotate to align first point vertically
                 x (* radius (Math/cos angle))
                 y (* radius (Math/sin angle))]
             [x y 0])))))

(def state 
  {:state {:type nil 
           :data nil}
   :drawing-deck {:position [0.1 0 0]
                  :cards deck-with-keys}
   :discard-deck {:position [-0.1 0 0]
                  :cards {}}
   :players {1 {:position [0 0 0]
                :cards {}}}
   :players-count 5
   :positions {:drawing-deck [-0.1 -0.001 -0.001]
               :discard-deck [0.1  -0.001 -0.001]
               :pentagon   {:points (pentagon-points 0.4)}
               :square     {:points []}
               :triangle {:points []}}})


;; Game starts ; kiosztani a kartyakat, elso player randomolasa, elso player lepese, kovetkezo player jobbra




(reg-event-db 
 :state/setup! 
 (fn [db []] 
   state))
