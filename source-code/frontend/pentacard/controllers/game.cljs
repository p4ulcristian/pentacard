(ns frontend.pentacard.controllers.game
  (:require [re-frame.core :refer [reg-event-db dispatch]]
            ["@react-spring/three" :refer [SpringValue]]))



(defn re-sort-cards [cards]
  (let [sorted-cards (sort-by
                      (fn [[card-id card-data]] (:index card-data))
                      cards)
        indexed-cards (vec (map-indexed
                            (fn [index [card-id card-data]]
                              {card-id (assoc card-data :index index)})
                            sorted-cards))]
    (reduce merge indexed-cards)))
             

(defn vec-to-map [the-vec]
  
  (reduce merge
          (map (fn [[k v]]
                 {k v})
               the-vec)))

(defn filter-origin [cards filter-value] 
  (vec-to-map
   (filter
    (fn [[card-id {:keys [origin]}]]
      (= filter-value origin))
    cards)))

(reg-event-db
 :cards/draw!
 (fn [db [_ id]] 
   (let [cards         (-> db :cards)
         card-drawed   (get cards id) 
         drawing-deck    (dissoc (filter-origin cards :drawing-deck) id)
         discard-deck    (filter-origin cards :discard-deck) 
         next-discard-index    (count discard-deck)
         card-modified        (assoc card-drawed
                                     :origin :discard-deck
                                     :index next-discard-index)
         new-discard-deck      (re-sort-cards (assoc discard-deck id card-modified)) 
         new-drawing-deck      (re-sort-cards drawing-deck)
         
         
         new-cards (merge new-drawing-deck
                          new-discard-deck)] 
     
     (-> db 
         (assoc-in [:cards] new-cards)))))



(reg-event-db
 :game/draw!
 (fn [db [_]]
   (let [cards (-> db :cards)
         drawing-deck  (filter-origin cards :drawing-deck)] 
     (when-not 
      (empty? drawing-deck)
      (let [last-card-id (first (last drawing-deck))]
        (dispatch [:cards/draw! last-card-id]))))))

(reg-event-db
 :cards/discard!
 (fn [db [_ id]]
   (let [cards         (-> db :cards)
         card-drawed   (get cards id)
         discard-deck    (dissoc (filter-origin cards :discard-deck) id)
         drawing-deck    (filter-origin cards :drawing-deck)
         next-discard-index    (count drawing-deck)
         card-modified        (assoc card-drawed
                                     :origin :drawing-deck
                                     :index next-discard-index)
         new-discard-deck      (re-sort-cards (assoc drawing-deck id card-modified))
         new-drawing-deck      (re-sort-cards discard-deck)


         new-cards (merge new-drawing-deck
                          new-discard-deck)]

     (-> db
         (assoc-in [:cards] new-cards)))))



(reg-event-db
 :game/discard!
 (fn [db [_]]
   (let [cards (-> db :cards)
         discard-deck (filter-origin cards :discard-deck)]
     (when-not
      (empty? discard-deck)
      (let [last-card-id (first (last discard-deck))]
        (dispatch [:cards/discard! last-card-id]))))))