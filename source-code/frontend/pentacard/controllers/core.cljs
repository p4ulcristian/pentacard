(ns frontend.pentacard.controllers.core
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
     :suit-emoji (get suits-emojis suit)}))

(reg-event-db 
 :set-default-cards 
 (fn [db _]
   (assoc db :drawing-deck deck)))

(reg-event-db
 :add-to-discard-deck
 (fn [db [_ card]]
   (let [drawing-deck (:drawing-deck db)
         discard-deck (:discard-deck db)]
     (-> db
         (assoc :drawing-deck (vec (remove (fn [this] (= this card))
                                           drawing-deck)))
         (assoc :discard-deck (conj discard-deck card))))))


(dispatch [:set-default-cards])