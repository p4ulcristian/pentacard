(ns frontend.router
  (:require ["react" :as react]
            [accountant.core :as accountant]
            [clerk.core :as clerk]
            [frontend.pentacard.core :as pentacard]
            [frontend.re-frame.events.api]
            [frontend.starter-kit.events.api]
            [frontend.starter-kit.subscriptions.api]
            [my-re-frame :refer [reg-event-db]]
            [re-frame.alpha :refer [dispatch subscribe]]
            [reagent.core :as reagent]
            [reitit.frontend :as reitit]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Mouse/Touch listeners  ;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def mouse-events ["mousedown" "mouseup" "mousemove"
                   "mouseout" "mouseleave" "mouseenter"])

(def touch-events ["touchstart" "touchmove" "touchend" "touchcancel"])

(defn mouse-position [event-handler]
  (fn [event]
    (event-handler
     {:x    (try
              (.-clientX event)
              (catch js/Object e 0))
      :y    (try
              (.-clientY event)
              (catch js/Object e 0))})))

(defn touch-position [event-handler]
  (fn [event]
    (let [touches   (or
                     (try
                       (.-touches event)
                       (catch js/Object e 0))
                     (try
                       (.-changedTouches event)
                       (catch js/Object e 0)))
          touch     (aget touches 0)]
      (event-handler
       {:x    (try
                (.-clientX touch)
                (catch js/Object e 0))
        :y    (try
                (.-clientY touch)
                (catch js/Object e 0))}))))

(def touch-fn-atom (atom nil))

(def mouse-fn-atom (atom nil))

(defn remove-touch-listeners [listener-names event-handler]
  (doseq [listener-name listener-names]
    (.removeEventListener js/document listener-name @touch-fn-atom)))

(defn remove-mouse-listeners [listener-names event-handler]
  (doseq [listener-name listener-names]
    (.removeEventListener js/document listener-name @mouse-fn-atom)))

(defn add-touch-listeners [listener-names event-handler]
  (reset! touch-fn-atom (touch-position event-handler))
  (doseq [listener-name listener-names]
    (.addEventListener js/document listener-name @touch-fn-atom)))

(defn add-mouse-listeners [listener-names event-handler]
  (reset! mouse-fn-atom (mouse-position event-handler))
  (doseq [listener-name listener-names]
    (.addEventListener js/document listener-name @mouse-fn-atom)))


(defn add-pointer-listeners [event-handler]
  (add-touch-listeners touch-events event-handler)
  (add-mouse-listeners mouse-events event-handler))

(defn remove-pointer-listeners [event-handler]
  (remove-touch-listeners touch-events event-handler)
  (remove-mouse-listeners mouse-events event-handler))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Window resize listeners  ;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn recalculate-window-size [e]
  (dispatch [:browser/set-window!]))

(defn remove-resize-listeners []
  (.removeEventListener js/document "resize" recalculate-window-size))

(defn set-pointer [e]
  (dispatch [:browser/set-pointer! e]))

(defn add-pointer-watcher []
  (add-pointer-listeners set-pointer))

(defn remove-pointer-watcher []
  (remove-pointer-listeners set-pointer))

(defn add-resize-listeners []
  (recalculate-window-size "")
  (.addEventListener js/visualViewport "resize" recalculate-window-size))



(reg-event-db 
 :router/go-to! 
 (fn [db [_ url]]
   (accountant/navigate! url)
   db))

(def router
  (reitit/router
   [["/"                                             :core]]))


(defn add-routing! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match          (reitit/match-by-path router path)
            current-page   (:name (:data  match))
            route-params   (:path-params match)]
        (when (= current-page :editor-scroll-snapshot)
          (let [{:keys [page-id snapshot-id]} route-params] 
            (dispatch [:snapshots/select 
                       (keyword page-id) 
                       (keyword snapshot-id)])))
        (when (= current-page :wizard)
          (dispatch [:editor/unselect!]))
        (reagent/after-render clerk/after-render!)
        (dispatch [:db/set [:wizard-page] {:current-page    current-page
                                           :params          route-params}])
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]

      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!))

(defn init-effect []
  (react/useEffect
   (fn [] 
     (dispatch [:animation/init-app!])
     (add-resize-listeners)
     (add-pointer-watcher)
     (add-routing!)
     (fn [] 
       (remove-resize-listeners)
       (remove-pointer-watcher)))
   #js []))

(defn- not-found-title []
  [:div 
       {:style {:font-size "3em"
                :font-weight :bold
                :padding-bottom "10px"}}
       "404"])

(defn- not-found-description []
  [:div
       {:style {:text-align :center
                :display :flex 
                :justify-content :center 
                :font-size "1em"
                :align-items :center}}
       [:div "This page doesn't exist."]])


(defn not-found-page []
  [:div 
   {:style {:height "100%"
            :width "100%"
            :display :flex 
            :justify-content :center 
            :align-items :center
            :background "#333"}}
   [:div {:style {:position :relative}} 
    [:img {:src "/images/not-found.webp"
           :style {:width "400px"
                   :max-width "100%"
                   :height "400px"
                   :border-radius "0px 100px 0px 100px / 0px 100px 0px 100px"}}]
    [:div.backdrop-filter
     {:style 
      {:position :absolute 
       :left "50%"
       :top "82%"
       :width "400px"
       :border-radius "0px 100px 0px 100px / 0px 100px 0px 100px"
       :padding "20px 30px"
       :background "rgba(255,255,255,0.5)" 
       :transform "translate(-50%, -50%)"}}
     [:div {:style {:display :flex
                    :gap "20px"}}
      [not-found-title]
      [not-found-description]]
     [:div 
      {:style { :text-align :right}}
      "You've discovered a hidden realm, but unfortunately, it's not the one you were seeking."]
     ]]])


(defn ui-router []
  (let [current-page (subscribe [:db/get [:wizard-page :current-page]])
        preview-mode? @(subscribe [:db/get [:preview-mode?]])]
    (init-effect) 
    [:div
     (case @current-page
       :core [pentacard/core]
       [not-found-page])]))
      

