(ns frontend.pentacard.events.animations.deal-cards
  (:require [re-frame.alpha :refer [dispatch reg-event-db]]
            [frontend.pentacard.events.animations.utils :as utils]
            ["@react-spring/three" :refer [SpringValue]]))


(defn animation-callback []
  (let [x-id (utils/id)
        y-id (utils/id) 
        z-id (utils/id)]))

(defn offset-by-index [position index]
  (let [[x y z] position
        new-x (if (= 0 (rem index 2))
                (- x 0.07)
                (+ x 0.07))
        new-y (+ 0.07 (+ y (* (quot index 2) -0.125)))] 
    [new-x new-y z]))

(defn one-deal-animation [db ref player-index card-index]
  (let [players-count (-> db :players-count)
        position (get-in db [:positions :players players-count player-index])
        [x y z] position
        [new-x new-y new-z] (offset-by-index position card-index)] 
       
    (dispatch [:render/add-animation {:from 0 
                                     :to new-x 
                                     :attribute "x"
                                     :ref ref}])
    (dispatch [:render/add-animation {:from 0
                                     :to new-y
                                     :attribute "y"
                                     :ref ref}])
    ;; (dispatch [:render/add-callback z-spring
    ;;            #(set! (-> ref .-current .-position .-z)
    ;;                   (.get z-spring))])))
    (set! (-> ref .-current .-rotation .-z) (* player-index (* 72 (/ (.-PI js/Math) 180))))
    ;(set! (-> ref .-current .-position .-x) x)
    ;(set! (-> ref .-current .-position .-y) new-y)
    (set! (-> ref .-current .-position .-z) 0)))


; [a b c d] -> [[0 a] [1 b] [2 c] [3 d]]

(defn deal-animation [db cards-by-player]
  
  (let [card-objects (get-in db [:objects :cards])]
    (doseq [[index [card-id card-data]] (map-indexed (fn [i a] [i a])
                                             cards-by-player)]  
      (let [ref (get-in card-objects [card-id])]
        (.setTimeout js/window 
                     #(one-deal-animation db ref (:origin card-data) (:index card-data)) 
                     (* index 100))))))
        


(reg-event-db
 :animation/deal-cards!
 ;; cards-by-player [[card-id card-id ...] [card-id card-id ...] ...]
 (fn [db [_ cards-by-player]]
   (let [] 
     (deal-animation db cards-by-player)
     db)))