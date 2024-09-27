(ns frontend.pentacard.controllers.game-steps
  (:require ["@react-spring/three" :refer [SpringValue]]
            [frontend.pentacard.controllers.game-steps.start :as start]
            [re-frame.alpha :refer [dispatch reg-event-db]]))


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
 :cards/start!
 (fn [db [_ id]]
   (let [cards         (-> db :cards)
         card-drawed   (get cards id)
         drawing-deck    (dissoc (filter-origin cards :drawing-deck) id)
         board-deck     (filter-origin cards :board-1)
         next-discard-index    (count board-deck)
         card-modified        (assoc card-drawed
                                     :origin :board-1
                                     :index next-discard-index)
         new-discard-deck      (re-sort-cards (assoc board-deck id card-modified))
         new-drawing-deck      (re-sort-cards drawing-deck)


         new-cards (merge new-drawing-deck
                          new-discard-deck)]

     (-> db
         (assoc-in [:cards] new-cards)))))


(reg-event-db
 :game/draw!
 (fn [db [_]]
   (start/start db)))

(reg-event-db
 :game/start!
 (fn [db [_]]
   (start/start db)))

(reg-event-db
 :cards/discard!
 (fn [db [_ id]]
   (let [cards         (-> db :cards)
         card-drawed   (get cards id)
         discard-deck    (dissoc (filter-origin cards :discard-deck) id)
         drawing-deck    (filter-origin cards :drawing-deck)
         next-discard-index    (count discard-deck)
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