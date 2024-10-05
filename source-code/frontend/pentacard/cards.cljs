(ns frontend.pentacard.cards
  (:require 
   ["@react-three/fiber" :refer [useLoader Canvas useFrame useLoader]]
   ["react" :as react] 
   [re-frame.alpha :refer [subscribe dispatch]]
   ["@react-three/drei" :refer [Box Plane Grid]]
   ["@react-three/drei" :refer [useGLTF Sky Environment PerspectiveCamera Html RoundedBox Box, OrbitControls
                                useBoundingBox
                                useTexture
                                RoundedBox
                                Center
                                GizmoViewport
                                GizmoHelper
                                Text Text3D]]
   ["three" :as THREE]
   ["@react-spring/three" :refer [useSpring useSpringValue, a]]))
            

(defn card [card-id card-data]
  (let [texture (useTexture "/images/logo.webp") 
        {:keys [index origin]} card-data
        origin-index? (number? origin)
        players-count @(subscribe [:db/get [:players-count]])
        ref (react/useRef)] 
    (react/useEffect 
     (fn []
       (let [position (-> ref .-current .-position)
             rotation (-> ref .-current .-rotation)
             [x y z] @(subscribe [:db/get 
                                  (if origin-index?
                                    [:positions :players players-count origin]
                                    [:positions origin])])] 
         (dispatch [:db/set [:objects :cards card-id] ref])
         ;(when origin-index? (aset rotation "z" (* origin (/ (.-PI js/Math) 3))))
         (aset position "x" x)
         (aset position "y" y)
         (aset position "z" (* index 0.005))
         )
       (fn []))
     #js [])
     ;#js [origin])
     
    [:mesh
     {:ref ref}
     [:> Box {:args [0.1 0.1 0.001]
              :castShadow true 
              :receiveShadow true}
      [:meshPhongMaterial {:map texture}]
      ]]))


(defn view []
  (let [board-ref (react/useRef) 
        rotation [0 0 0]
        cards @(subscribe [:db/get [:game :cards]])]
   ;;  (useFrame
   ;;   (fn []
   ;;     (set! (-> box-ref .-current .-rotation .-x)
   ;;           (+ (-> box-ref .-current .-rotation .-x) 0.004))
   ;;     (set! (-> box-ref .-current .-rotation .-y)
   ;;           (+ (-> box-ref .-current .-rotation .-y) 0.004))
   ;;     (set! (-> box-ref .-current .-rotation .-z)
   ;;           (+ (-> box-ref .-current .-rotation .-z) 0.004))))
    [:group {:rotation rotation
             :ref board-ref} 
     (map 
      (fn [[card-id card-data]] 
        ^{:key card-id}[card card-id card-data])
      cards)]))

     
     