(ns frontend.developer-tools.core
  (:require ["react" :as react]
            [clojure.string :as clojure-string]
            [frontend.wizard.ui.style :as style]
            [frontend.wizard.utils.common :as utils]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]))



(defn copy-to-clipboard [text]
  (let [promise   (.writeText js/navigator.clipboard text)]
    (-> promise
      (.then (fn [] (println "Copying to clipboard was successful!")))
      (.catch (fn [err] (println "Could not copy text:" err))))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; State viewer ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn value-style [color]
  {:cursor           :pointer
   :padding          "5px"
   :margin-top       "10px"
   :border-radius    "5px"
   :background       color
   :position         :relative
   :height           :auto
   :overflow-wrap    "anywhere"
   :width            "fit-content"
   :min-width        "200px"
   :user-select      "all"})



(defn state-viewer--counter [sub-tree]
  (when (or (map? sub-tree) (vector? sub-tree))
    [:div {:style    {:position           :absolute
                      :top                -5
                      :right              -15
                      :height             "15px"
                      :width              "15px"
                      :display            :flex
                      :justify-content    :center
                      :align-items        :center
                      :background         "lightblue"
                      :border-radius      "50%"
                      :padding            "2px"
                      :font-size          "12px"
                      :font-weight        :bold
                      :border             "0.5px solid black"}}
     (count sub-tree)]))



(defn vector-item [item]
  [:div {:style    {:padding    "5px"}} (str item)])



(defn state-viewer--vector-display [_recur-fn tree _path]
  [:div {:style    (value-style "lightblue")}
   [state-viewer--counter tree]
   [:div
    "["
    (clojure-string/join " "
      (map (fn [e] (str e))
        tree))
    "]"]])



(defn state-viewer--keyword-display [recur-fn tree path]
  [:div {:style    (value-style "#FF77FF")}
   (str tree)])



(defn remove-button [path]
  [:button
   {:style       {:background    :red}
    :on-click    (fn [e] (dispatch [:db/set (rest path) nil]))}
   "x"])



(defn state-viewer--map-display-item [recur-fn the-key sub-tree path]
  (let [open?   (r/atom (= path [:root]))]
    (fn [recur-fn the-key sub-tree path]
      [:div {:style    {:margin    "10px 0px"}}
       [:div {:style       (value-style "#FFEA00")
              :on-click    (fn [e]
                          ;(copy-to-clipboard (str sub-tree))
                             (reset! open? (not @open?)))}
        [:div {:on-click #(println "State viewer log: " sub-tree)
               :style    {:display            :flex
                          :justify-content    :space-between
                          :pointer-events     :auto}}
         (str the-key)
         [remove-button path]]
        [state-viewer--counter sub-tree]]
       (when @open?
         [:div {:style    {:margin-left    "50px"}}
          [recur-fn sub-tree path]])])))



(defn state-viewer--map-display [recur-fn tree path]
  [:<> (map (fn [[the-key sub-tree]]
              (let [new-path       (vec (conj path the-key))
                    new-sub-tree   (utils/map->sorted-map sub-tree)]
                ^{:key    (str new-path)}
                [state-viewer--map-display-item recur-fn the-key new-sub-tree new-path]))
         tree)])



(defn simple-value [tree]
  [:div {:style    {:text-align    :center}}
   (str tree)])



(defn state-viewer--recursion [tree path]
  [:<>
   (cond
     (vector? tree)   ^{:key    (str path)} [state-viewer--vector-display state-viewer--recursion  tree path]
     (map? tree)      ^{:key    (str path)} [state-viewer--map-display    state-viewer--recursion  tree path]
     (keyword? tree)  ^{:key    (str path)} [state-viewer--keyword-display state-viewer--recursion tree path]
     (= tree nil)     ^{:key    (str path)} [:div {:style    (value-style "red")}   "nil"]
     (= tree true)    ^{:key    (str path)} [:div {:style    (value-style "green")} "true"]
     :else            ^{:key    (str path)} [:div {:style    (value-style "white")} [simple-value tree]])])



(def excluded-keys [:browser])

(defn exclude-keys [state]
  (reduce (fn [new-state excluded-key] 
            (dissoc new-state excluded-key))
          state
          excluded-keys))


(defn state-viewer--recursion-wrapper []
  (let [state (subscribe [:db/get []])]
    [:div 
     [state-viewer--recursion
      (utils/map->sorted-map (exclude-keys @state)) 
      []]]))



(defn state-viewer--transparency []
  (let [transparency-path   [:state-viewer :transparency]
        transparency        @(subscribe [:db/get transparency-path])]
    [:div {:on-click    #(dispatch [:db/set  transparency-path (not transparency)])
           :style       {:position       :fixed
                         :top            0
                         :right          30
                         :color          :white
                         :font-weight    :bold
                         :font-size      "30px"
                         :padding        "10px"
                         :cursor         :pointer}} "T "]))



(defn state-viewer--close []
  [:div {:on-click    #(dispatch [:db/set [:state-viewer :open?] false])
         :style       {:position       :fixed
                       :top            0
                       :right          0
                       :color          :white
                       :font-weight    :bold
                       :font-size      "30px"
                       :padding        "10px"
                       :cursor         :pointer}} "X"])



(defn state-viewer--container [content]
  (let [state-viewer?       @(subscribe [:db/get [:state-viewer :open?]])
        transparency-path   [:state-viewer :transparency]
        transparency        @(subscribe [:db/get transparency-path])]
    (when state-viewer?
      [:div {:style    {:position          :fixed
                        :height            "100dvh"
                        :width             "100dvw"
                        :background        "#222"
                        :left              0
                        :opacity           (if transparency 0.7 1)
                        :pointer-events    (if transparency :none :auto)
                        :overflow-x        "auto"
                        :top               0
                        :z-index           (style/z-index :level-max)
                        :overflow-y        :auto
                        :padding           "30px"
                        :color :black
                        :box-sizing        "border-box"}}
       content])))



(defn state-viewer--menu [content]
  [:div {:style    {:pointer-events    :auto}}
   content])



(defn load-in-wizard-config []
  (let [[value               set-value]  (react/useState)]
    [:div
     {:style    {:background        "#333"
                 :color             :white
                 :display           :flex
                 :flex-direction    :column
                 :width             "400px"}}
     [:input {:on-change      #(set-value (-> % .-target .-value))
              :placeholder    "Transaction ID"
              :style          {:color         :black
                               :text-align    :center
                               :padding       "10px"}}]
     [:button {:on-click    #(dispatch [:bundlr/download-wizard-config value])
               :style       {:padding    "10px"}}
      "Download config"]]))



(defn export-wizard-config []
  (let [pages @(subscribe [:db/get [:pages]])
        input-ref (react/useRef)
        [page set-page] (react/useState)]
    [:div {:style {:color :white
                   :display :flex
                   :flex-direction :column
                   :width :fit-content
                   :padding "10px"
                   :gap "20px"}}
     [:input {:ref input-ref
              :value page
              :style {:color "#333"}}]
     (map
       (fn [[page-key page-value]]
         [:div
          {:on-click #(do
                        (set-page (str {page-key page-value}))
                        (.select (.-current input-ref))
                        (.setTimeout js/window (fn [e]
                                                 (.setSelectionRange (.-current input-ref) 0 99999)
                                                 (copy-to-clipboard (str (.-value (.-current input-ref))))
                                                 (.alert js/window (str "Copied" page-key " to clipboard")))
                          200))
           :style {:background "#666"
                   :border-radius 20
                   :padding "10px"
                   :cursor :pointer}}
          (str "Export: " page-key)])
       pages)]))



(defn view []
  [state-viewer--container
   [:<>
    [state-viewer--menu
     [:<>
      [state-viewer--close]
      [state-viewer--transparency]]]
    [state-viewer--recursion-wrapper]]])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; State viewer ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
