(ns frontend.re-frame.events.dropzones.helpers
  (:require [frontend.re-frame.events.dropzones.math :as math]
            [frontend.wizard.utils.areas :as areas]
            [frontend.wizard.utils.common :as wizard-utils]
            [frontend.wizard.utils.nodes :as nodes]
            [re-frame.core :refer [subscribe]]))


(defn set-new-overlay-config [db {:keys [impossible-config? overlapped-paths area dragged-path]}]
  (let [overlay-config-path   [:overlays :areas]
        overlay-config        (get-in db overlay-config-path)
        area-dropzones        (get overlay-config :area-dropzones)
        
        new-overlay-config    (merge overlay-config
                                     {:impossible-config?    impossible-config?
                                      :overlapped            overlapped-paths
                                      :active                area
                                      :dragged               area
                                      :dragged-path          dragged-path
                                      :no-dropzones?        (and 
                                                             (not-empty area-dropzones)
                                                             (empty? overlapped-paths))})] 
    (assoc-in db overlay-config-path new-overlay-config)))

(defn get-dragged-path [path area]
  (let [sub-nodes             (nodes/path->sub-nodes path)
        dragged-key           (nodes/area-position->node-key
                               (areas/letter-to-number area)
                               sub-nodes)]
    (vec (concat path [:components dragged-key]))))


(defn get-areas-and-coords--deepest-path [overlapped-areas-and-coords]
  (let [overlapped-paths     (mapv second (mapv first overlapped-areas-and-coords)) 
        deepest-path         (wizard-utils/get-deepest-path overlapped-paths)
        deepest-overlapped   (->>
                              overlapped-areas-and-coords
                              (filter (fn [[[this-index this-path] coords]]
                                        (= this-path deepest-path))))] 
    deepest-overlapped))



(defn get-overlapped--same-path [overlapped-areas-and-coords path]
  (filter (fn [[[this-index this-path] coords]]
            (= this-path path))
          overlapped-areas-and-coords))




(defn get-overlapping--pointer []
  (let [pointer-x @(subscribe [:browser/pointer-x])
        pointer-y @(subscribe [:browser/pointer-y])
        overlapped-coordinates   (keep (fn [[dropzone-index dropzone-coordinates]]
                                         (let [[A B C D] dropzone-coordinates
                                               [left top]  A
                                               [right bottom] D]
                                           (when (and (< left pointer-x right) (< bottom pointer-y top))
                                             [dropzone-index dropzone-coordinates])))
                                       @(subscribe [:db/get [:overlays :areas :area-dropzones]]))
        overlapped-areas         (mapv first overlapped-coordinates)
        overlapped-paths         (mapv second overlapped-areas)
        deepest-path             (wizard-utils/get-deepest-path overlapped-paths)
        deepest-overlapped       (->>
                                  overlapped-areas
                                  (filter (fn [[this-index this-path]]
                                            (= (count this-path)
                                               (count deepest-path)))))
        overlapping-area                        (first deepest-overlapped)]
    ;It gives back [[1 [:pages :random-key]]] 
    (if overlapping-area
      [overlapping-area]
      [])))



(defn correct-dimensions [{:keys [left top width height x y]}]
  (let [new-width (wizard-utils/zoom width)
        new-height (wizard-utils/zoom height)
        new-left (+ (wizard-utils/zoom x) left)
        new-top  (+ (wizard-utils/zoom y) top)]
    {:width new-width
     :height new-height

     :left    new-left
     :right  (+ new-left new-width)
     :top     new-top
     :bottom (+ new-top new-height)}))


(defn get-overlapping-without-dragged [area-dropzones dragged-path]
  (vec (filter (fn [[[this-index this-path] coordinates]]
                 (not (or (=
                           (vec (drop-last 2 this-path))
                           dragged-path)
                          (wizard-utils/vec-one-contains-vec-two? this-path dragged-path))))
               area-dropzones)))



(defn get-overlapped-areas-and-coords [area-dropzones examined-rect]
  (let [filter-func              (fn [[_ potential-rect]]
                                   (math/more-than-some-percent-area? 0.5 examined-rect potential-rect))
        overlapped-areas-and-coords         (filter filter-func area-dropzones)]
     overlapped-areas-and-coords))


(defn get-overlapped-by-completed-rect-areas-and-coords [areas-and-coords area-dropzones]
  (let [completed-rect              (math/calculate-complete-rect areas-and-coords)
        overlapped-by-completed-rect-areas-and-coords 
        (get-overlapped-areas-and-coords area-dropzones completed-rect)] 
    overlapped-by-completed-rect-areas-and-coords))

(defn get-overlapped-by-dragged-areas-and-coords-on-level [areas-and-coords]
  (let [dragged-dimensions          @(subscribe [:fake-dragged/dimensions]) 
        examined-coordinates        (-> dragged-dimensions correct-dimensions wizard-utils/rect-to-coordinates)
        overlapped-areas-and-coords (get-overlapped-areas-and-coords areas-and-coords examined-coordinates)]
    overlapped-areas-and-coords))

(defn get-areas-and-coords-without-dragged [area-dropzones]
  (let [dragged-path             @(subscribe [:db/get [:overlays :areas :dragged-path]])]
   (get-overlapping-without-dragged area-dropzones dragged-path)))



(defn get-overlapping--rect-deepest-level []
  (let [area-dropzones           @(subscribe [:db/get [:overlays :areas :area-dropzones]])] 
    (mapv first
          (-> area-dropzones 
              get-overlapped-by-dragged-areas-and-coords-on-level 
              get-areas-and-coords-without-dragged
              get-areas-and-coords--deepest-path
              (get-overlapped-by-completed-rect-areas-and-coords area-dropzones) 
              get-areas-and-coords-without-dragged
              get-areas-and-coords--deepest-path)))) 
              
              

(defn get-overlapping--rect-same-level []
  (let [dragged-path             @(subscribe [:db/get [:overlays :areas :dragged-path]])
        area-dropzones           @(subscribe [:db/get [:overlays :areas :area-dropzones]])] 
    (mapv first 
      (-> area-dropzones 
          get-overlapped-by-dragged-areas-and-coords-on-level
          get-areas-and-coords-without-dragged
          (get-overlapped--same-path (drop-last 2 dragged-path))
          ( get-overlapped-by-completed-rect-areas-and-coords area-dropzones)
          get-areas-and-coords-without-dragged
          (get-overlapped--same-path (drop-last 2 dragged-path))))))
            




(defn get-overlapping-areas-on-move []
  (let [overlapped-by-rect        (get-overlapping--rect-deepest-level)
        overlapped-by-pointer     (get-overlapping--pointer)
        any-overlapped-by-rect?   (not-empty overlapped-by-rect)
        final-overlapped          (if any-overlapped-by-rect?
                                    overlapped-by-rect
                                    overlapped-by-pointer)]
    final-overlapped))

(defn get-overlapping-areas-on-resize []
  (let [overlapped-by-rect        (get-overlapping--rect-same-level)
        overlapped-by-pointer     (get-overlapping--pointer)

        any-overlapped-by-rect?   (not-empty overlapped-by-rect)
        final-overlapped          (if any-overlapped-by-rect?
                                    overlapped-by-rect
                                    overlapped-by-pointer)]
    final-overlapped))