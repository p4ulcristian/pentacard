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
            


(defn get-coordinates [board-ref card-ref position]
  (let [ref @(subscribe [:db/get [:objects :discard-deck :ref]])
        deck-world-position    (new  THREE/Vector3)
        card-world-position    (new THREE/Vector3)
        a (.getWorldPosition (.-current ^js ref) deck-world-position) 
        new-position (.worldToLocal (.-current ^js board-ref) deck-world-position)] 
    
   
    (.log js/console "hello" new-position)
    [(.-x new-position)
     (.-y new-position)
     (.-z new-position)]))
   ; (.log js/console (.getWorldPosition (.-current ^js ref) deck-world-position))))




(defn appear-animation [ref [x y z]]
  (let [delay 0.5
        x-rotation (useSpringValue  (.-PI js/Math)  #js {:to 0
                                                         :delay delay
                                                         :duration 1000
                                                         :config #js {:mass 5}}) 
        x-spring (useSpringValue (+ 0.5 x) #js {:to x
                                                :delay delay
                                                :duration 1000
                                                :config #js {:mass 5}}) 
        z-spring (useSpringValue (- z 1) #js {:to z
                                              :delay delay
                                              :duration 1000
                                              :config #js {:mass 0.5}})]
    (useFrame (fn []
                (let [position (-> ref .-current .-position)
                      rotation (-> ref .-current .-rotation)]
                  (aset position "x" (.get x-spring))
                  (aset rotation "x" (.get x-rotation))
                  (aset position "y" y)
                  (aset position "z" (.get z-spring)))))))
                  
(defn card-stab-animation [board-ref ref [old-x old-y old-z] stabbed?] 
  (let [x-spring (useSpringValue old-x #js {:delay 1000
                                            :duration 1000
                                            :config #js {:mass 10}})
        y-spring (useSpringValue old-y #js {:delay 1000
                                            :duration 1000
                                            :config #js {:mass 0.5}})
        z-spring (useSpringValue old-z #js {:delay 1000
                                            :duration 1000
                                            :config #js {:mass 20}
                                            :onResolve (fn [a b] (.start ^js b 0))})
        rotation-x-spring   (useSpringValue 0 #js {:delay 1000
                                                   :duration 1000
                                                   :config #js {:mass 0.5}})
        rotation-y 0
        [first? set-first?] (react/useState true)]
    (useFrame (fn []
                (when stabbed?
                  (let [new-position         (get-coordinates board-ref ref [old-x old-y old-z])
                        [new-x new-y new-z]  new-position
                        position (-> ref .-current .-position)
                        rotation (-> ref .-current .-rotation)]
                    (when first?
                      (.start x-spring new-x)
                      (.start y-spring new-y) 
                      (.start z-spring (- (rand-nth (map (fn [a] (/ a 1000))
                                                         (range 100))))) 
                      (.start rotation-x-spring 0))
                    (set-first? false)
                    (aset position "x" (.get x-spring))
                    (aset position "y" (.get y-spring))
                    (aset position "z" (.get z-spring))
                    (aset rotation "x" (.get rotation-x-spring))
                    (aset rotation "y" rotation-y)))))))

(defn card-placeholder [board-ref position]
  (let [texture (useTexture "/images/logo.webp") 
        pos-ref (react/useRef)
        [stabbed? set-stabbed?] (react/useState false)] 
    (appear-animation pos-ref position)
    (card-stab-animation board-ref pos-ref position stabbed?)
    [:mesh
     {:ref pos-ref 
      :onPointerDown (fn [] 
                       (set-stabbed? true))}
     [:> Box {:args [0.1 0.1 0.01]
              :castShadow true 
              :receiveShadow true}
      [:meshPhongMaterial {:map texture}]]]))


(defn player-cards []
  (let [board-ref (react/useRef) 
        rotation [0 0 0]
        card-placeholders @(subscribe [:db/get [:card-placeholders]])]
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
     (comment
       (map 
        (fn [[key {:keys [position]}]] 
          [card-placeholder board-ref position])
        card-placeholders))]))

     
     