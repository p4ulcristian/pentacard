(ns frontend.re-frame.events.components.event-helpers
  (:require 
   [frontend.wizard.utils.areas :as areas] 
   [frontend.wizard.utils.nodes :as nodes]
   [frontend.wizard.utils.common :as wizard-utils]
   [re-frame.alpha :refer [dispatch]]))

;;; helpers 


(defn get-overlapped-indexes-and-paths [db]
  (get-in db [:overlays :areas :overlapped]))


(defn get-overlapped-area-indexes [db]
  (mapv first (get-overlapped-indexes-and-paths db)))


(defn get-filtered-overlapped-indexes [db path]
  (let [overlapped-indexes-and-paths (get-overlapped-indexes-and-paths db)] 
    (->> overlapped-indexes-and-paths
         (filter (fn [[_ this-path]]
                   (= this-path path)))
         (mapv first))))


(defn select-new-layer [db path layer-key]
  (let [frame-path (vec (drop-last 2 path))] 
    (dispatch [:layers/select! frame-path layer-key])
    (-> db 
        (assoc-in [:editor :active-layers frame-path] layer-key))))


(defn get-deepest-overlapped-path [db]
  (let [overlapped-indexes-and-paths (get-overlapped-indexes-and-paths db)
        overlapped-paths     (mapv second overlapped-indexes-and-paths)]
    (wizard-utils/get-deepest-path overlapped-paths)))

(defn get-new-layer-path [path new-layer-key]
  (let [layers-path (vec (butlast path))]
    (conj layers-path new-layer-key)))

;;; conditionals 

(defn source-contains-target? [source-path target-path]
  (let [source-path-length     (count source-path)
        target-path-length     (count target-path)
        source-path-shorter?   (< source-path-length target-path-length)]
    source-path-shorter?))

(defn is-resized? [db]
  (get-in db [:overlays :areas :resized]))

(defn is-impossible-config? [db]
  (-> db :overlays :areas :impossible-config?))


;;; nodes



(defn remove-unused-nodes [db path new-areas]
  (let [old-areas           (nodes/path->areas path)
        removed-positions   (areas/compare-areas--get-deleted-positions old-areas new-areas)]
    (nodes/remove-nodes-at-positions db path  removed-positions)))


(defn get-node-path-by-area [path area]
  (let [position    (areas/letter-to-number area)
        sub-nodes   (nodes/path->sub-nodes path)
        area-key    (nodes/area-position->node-key position sub-nodes)]
    (vec (concat path [:components area-key]))))

(defn get-node-by-area [db path area] 
  (get-in db (get-node-path-by-area path area)))


(defn generate-node-name [type next-position]
  (case type
    "frame"  (str "Frame")
    "grid"   (str "Grid "   (inc next-position))
    "block"  (str "Blocks " (inc next-position))
    "text"   (str "Text "   (inc next-position))
    "image"  (str "Image "  (inc next-position))
    "unknown"))



;;; areas

(defn add-to-areas [db path]
  (let [areas                (nodes/path->areas path)
        next-area            (nodes/path->next-area path)
        indexes-overlapped   (get-filtered-overlapped-indexes db path)]
    (areas/add-area
     {:area         next-area
      :areas        areas
      :indexes      indexes-overlapped})))

(defn add-to-resetted-areas [db path]
  (let [areas               (nodes/path->areas path)
        indexes-overlapped  (get-filtered-overlapped-indexes db path)] 
    (areas/add-area-to-resetted
     {:area         "a"
      :areas        areas
      :indexes      indexes-overlapped})))

(defn set-new-areas [db path new-areas]
  (let [areas-path (nodes/path->areas-path path)]
    (assoc-in db areas-path new-areas)))

(defn get-new-areas-after-add [db path]
  (let [areas-path           (nodes/path->areas-path path)
        area-to-add          (areas/number-to-letter (count (nodes/path->sub-nodes path)))
        areas                (get-in db areas-path)
        indexes-overlapped   (get-overlapped-area-indexes db)
        new-areas            (areas/add-area
                               {:area       area-to-add
                                :areas      areas
                                :indexes    indexes-overlapped})]
    new-areas))



(defn get-new-areas-after-remove [db path area]
  (let [areas-path   (nodes/path->areas-path path)
        areas        (get-in db areas-path)
        new-areas    (areas/remove-area
                       {:area    area
                        :areas   areas})]
    new-areas))



(defn get-new-areas-after-move [db path area]
  (let [areas-path                       (nodes/path->areas-path path)
        areas                            (get-in db areas-path)
        indexes-overlapped--same-level   (get-filtered-overlapped-indexes db path)
        new-areas                        (areas/move-area
                                           {:area      area
                                            :areas     areas
                                            :indexes   indexes-overlapped--same-level})]
    new-areas))




(defn get-removed-node-positions-after-move [db path area]
  (let [areas-path   (nodes/path->areas-path path)
        areas        (get-in db areas-path)
        new-areas    (get-new-areas-after-move db path area)]
    (areas/compare-areas--get-deleted-positions areas new-areas)))




;; modifying functions

(defn move-section [db source-path target-path area]
  (let [section--source-path   (vec (concat source-path []))
        nodes--target-path     (vec (concat target-path [:components]))
        section--source        (get-in db section--source-path)
        nodes--target          (get-in db nodes--target-path)

        area-position-to-add   (count (nodes/path->sub-nodes target-path))

        node--key              (keyword (str (random-uuid)))
        new-nodes-target       (nodes/fix-nodes-order
                                (nodes/rename-keys-in-tree
                                 (assoc  nodes--target node--key
                                         (-> section--source
                                             (assoc-in [:style :position] area-position-to-add)
                                             (dissoc :height)
                                             (dissoc :width)
                                             (dissoc :section-root?)))))]

    (-> db
        (assoc-in nodes--target-path new-nodes-target))))



(defn add-node [db path component]
  (let [copying-element?       (get-in db [:editor :copying-element?])
        nodes-path             (vec (concat path [:components]))
        nodes                  (get-in db nodes-path)

        area-position-to-add   (count (nodes/path->sub-nodes path))

        node--key              (keyword (str (random-uuid)))

        new-nodes              (nodes/fix-nodes-order
                                (assoc  nodes node--key
                                        (-> component
                                            (assoc-in [:style :position] area-position-to-add)
                                            (assoc-in [:name] (generate-node-name (:type component) area-position-to-add)))))]

    ;; (cond
      ;; (and path
      ;;      (= (:type component) "custom-html")
      ;;      (not copying-element?))
      ;; (do
      ;;   (dispatch [:editor/select-component! (vec (conj nodes-path node--key))])
      ;;   (dispatch [:menu/select 2 :iframe-content]))

      ;; (and path
      ;;      (= (:type component) "image")
      ;;      (not copying-element?))
      ;; (do 
      ;;   ;(dispatch [:db/tap (vec (conj nodes-path node--key))])
      ;;   (dispatch [:editor/select-component! (vec (conj nodes-path node--key))])
      ;;   (dispatch [:menu/select 2 :background-url])))
      ;We need to stop using modals. We need the menu to open
      ;(do
      ;  (dispatch [:db/merge [:bundlr] {:path    (vec (conj nodes-path node--key))}])
      ;  (dispatch [:chakra/open-modal! "image-modal"])))
    
    (-> db
        (assoc-in nodes-path new-nodes))))


(defn copy-node [db source-path target-path area]
  (let [nodes--target-path     (vec (concat target-path [:components]))
        nodes--target          (get-in db nodes--target-path)

        node--source-path      (get-node-path-by-area source-path area)
        area-position-to-add   (count (nodes/path->sub-nodes target-path))

        node                   (get-in db node--source-path)
        new-nodes-target       (nodes/fix-nodes-order
                                (assoc  nodes--target (keyword (str (random-uuid)))
                                        (assoc-in (nodes/rename-keys-in-tree node)
                                                  [:style :position] area-position-to-add)))]
    (assoc-in db nodes--target-path new-nodes-target)))

(defn move-node [db source-path target-path area]
  (let [source-in-target?    (source-contains-target? source-path target-path)
        source-nodes-path    (nodes/path->nodes-path source-path)
        target-nodes-path    (nodes/path->nodes-path target-path)
        source-nodes         (get-in db source-nodes-path)
        target-nodes         (get-in db target-nodes-path {})
        moved-node-path      (get-node-path-by-area source-path area)
        next-position        (nodes/path->next-position target-path)

        node--key              (last moved-node-path)
        node                   (get-in db moved-node-path)
        new-node               (assoc-in node [:style :position] next-position)

        source-new-nodes       (nodes/fix-nodes-order (dissoc source-nodes node--key))
        target-new-nodes       (nodes/fix-nodes-order (assoc  target-nodes node--key new-node))] 
    (if
     source-in-target?
      (-> db
          (assoc-in source-nodes-path source-new-nodes)
          (assoc-in target-nodes-path target-new-nodes))
      (-> db
          (assoc-in target-nodes-path target-new-nodes)
          (assoc-in source-nodes-path source-new-nodes)))))
        