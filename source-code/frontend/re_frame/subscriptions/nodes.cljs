(ns frontend.re-frame.subscriptions.nodes
  (:require [re-frame.alpha :refer [reg-sub]]
            [frontend.wizard.utils.areas :as area-utils]
            [frontend.wizard.utils.nodes :as node-utils]))


(defn get-node [db path]
  (get-in db path))

(defn get-type [db path]
  (let [node (get-node db path)]
    (:type node)))

(defn get-layers [db path]
  (let [node (get-node db path)]
    (:layers node)))

(defn get-content [db path]
  (let [node (get-node db path)]
    (:content node)))

(defn is-parent-node-a-grid? [db path]
  (let [parent-node (get-node db (vec (drop-last 2 path)))]
    (= (:type parent-node) "grid")))

(defn get-style [db path]
  (let [node (get-node db path)]
    (:style node)))

(defn get-areas [db path]
  (let [style (get-style db path)]
    (:areas style)))

(defn get-cols [db path]
  (let [style (get-style db path)]
    (:cols style)))

(defn get-rows [db path]
  (let [style (get-style db path)]
    (:rows style)))


(defn get-area [db path]
  (let [style      (get-style db path)
        position   (:position style)
        area       (area-utils/number-to-letter position)]
    area))

(defn get-area-path [db path area]
  (let [nodes-path      (vec (concat path [:components]))
        nodes           (get-in db nodes-path)
        area-position   (area-utils/letter-to-number area)
        node-key        (node-utils/area-position->node-key area-position nodes)
        node-path       (vec (concat nodes-path [node-key]))]
    node-path))

(defn get-sub-nodes [db path]
  (reduce merge
          (map (fn [[id value]] {id    value})
               (sort-by
                (fn [[id comp]]
                  (let [new-path     (concat path [:components id])
                        this-style   (get-style db new-path)
                        position     (:position this-style)]
                    position))
                (:components (get-node db path))))))

(defn get-root? [db path]
  (let [parent-path (vec (drop-last 2 path))
        parent-node (get-node db parent-path)]
    (contains? parent-node :pages)))


(reg-sub
 :nodes/root?
 (fn [db [_ path]]
   (get-root? db path)))

(reg-sub
 :nodes/sub-nodes
 (fn [db [_ path]]
   (get-sub-nodes db path)))

(reg-sub
 :nodes/cols
 (fn [db [_ path]]
   (get-cols db path)))

(reg-sub
 :nodes/rows
 (fn [db [_ path]]
   (get-rows db path)))

(reg-sub
 :nodes/areas
 (fn [db [_ path]]
   (get-areas db path)))

(reg-sub
 :nodes/clean-areas
 (fn [db [_ path]]
   (mapv (fn [row]
           (mapv 
            (fn [row-item] ".")
            row)) 
         (get-areas db path))))


(reg-sub
 :nodes/area-type
 (fn [db [_ path area]]
   (let [node-path (get-area-path db path area)
         node      (get-in db node-path)
         type      (:type node)]
        type)))

(reg-sub 
 :nodes/area-path 
 (fn [db [_ path area]]
   (get-area-path db path area)))



(reg-sub
 :nodes/area
 (fn [db [_ path]]
   (get-area db path)))

(reg-sub
 :nodes/type
 (fn [db [_ path]]
   (get-type db path)))

(reg-sub
 :nodes/style
 (fn [db [_ path]]
   (get-style db path)))

(reg-sub 
 :nodes/is-parent-node-a-grid? 
 (fn [db [_ path]]
   (is-parent-node-a-grid? db path)))

(reg-sub
 :nodes/content
 (fn [db [_ path]]
   (get-content db path)))

(reg-sub
 :nodes/layers
 (fn [db [_ path]]
   (get-layers db path)))
