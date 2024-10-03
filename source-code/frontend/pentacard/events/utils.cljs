(ns frontend.pentacard.events.utils)

(defn filter-by-origin [cards origin]
  (filter
   (fn [[card-id card-data]]
     (= (:origin card-data) origin))
   cards))