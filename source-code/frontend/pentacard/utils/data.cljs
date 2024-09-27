(ns frontend.pentacard.utils.data)

(defn vector-pairs->map [shuffled-map]
  (reduce
   merge
   (map
    (fn [[k v]] {k v})
    shuffled-map)))

;clojure sort example


(defn re-index-with-same-order [coll]
  (let [sorted-by-index (sort
                         (fn [[id-one data-one] [id-two data-two]]
                           (let [index-one (:index data-one)
                                 index-two (:index data-two)]
                             (compare index-one index-two)))
                         coll)
        re-indexed-coll (map-indexed
                         (fn [index [id data]]
                          {id (assoc data :index index)})
                         sorted-by-index)]
    (reduce merge re-indexed-coll)))