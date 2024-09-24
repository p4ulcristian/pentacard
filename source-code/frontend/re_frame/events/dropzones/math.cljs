(ns frontend.re-frame.events.dropzones.math 
  (:require [frontend.starter-kit.utils.basic :as utils]))


(defn more-than-some-percent-area?
  ^{:doc    "Examining the overlap of rect-one and rect-two
1. Where rect-one and rect-two are rectangles
2. We are searching for the area of the overlapping rect-one and rect-two
3. We decide if the overlapped area is bigger then 50%
4. We iterate through a list of rects to decide which ones are overlapped more than 50%

Let's make some variable names clear.

   A -------- B
   |          |
   |          |
   C -------- D

       and

   E -------- F
   |          |
   |          |
   G -------- H

These are the points of the rectangle, accordingly Ax, Ay ... Dx, Dy
The first rectangle (A, B, C ,D) is the one which is overlapped by (E, F, G, H)"}
  [threshold-percent
   [[Ax Ay] [Bx By] [Cx Cy] [Dx Dy] :as original-coords]
   [[Ex Ey] [Fx Fy] [Gx Gy] [Hx Hy] :as examined-coords]]
  (let [examined-height            (- Ey Gy)
        examined-width             (- Fx Ex)
        overlapped-height          (max 0 (- (min Ay Ey) (max Cy Gy)))
        overlapped-width           (max 0 (- (min Bx Fx) (max Ax Ex)))
        examined-area              (* examined-height examined-width) 
        overlapped-area            (* overlapped-height overlapped-width)
        area-overlap>50-percent?
        (<= threshold-percent (/ overlapped-area examined-area))]
    area-overlap>50-percent?))



(defn calculate-complete-rect [areas-and-coords]
  (let [only-coords (mapv second areas-and-coords)
        [first-A  _   _  first-D] (first only-coords)
        [first-left    first-top] first-A 
        [first-right  first-bottom] first-D
        final-direction-coordinates
        (reduce
         (fn [result this]
           (let [[this-A _  _ this-D]       this
                 [this-left this-top]       this-A
                 [this-right this-bottom]   this-D
                 [result-left result-right result-top result-bottom]  result]
             [(min this-left   result-left)
              (max this-right  result-right)
              (max this-top    result-top)
              (min this-bottom result-bottom)]))
         [first-left first-right first-top first-bottom]
         only-coords)
        [final-left final-right final-top final-bottom] final-direction-coordinates
        final-rect-coords [ [final-left final-top]
                            [final-right final-top]
                            [final-left final-bottom]
                            [final-right final-bottom]]] 
    final-rect-coords))