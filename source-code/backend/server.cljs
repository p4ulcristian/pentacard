
(ns backend.server
  (:require ["express" :as express] 
            ["node:process" :as process]
            [backend.html.core :as html] 
            ["compression" :as compression] 
            [config   :refer [port]]
            [backend.env :as env] 
            ))


(.on process
     "uncaughtException" (fn [err origin]
                           (.log js/console "Uncaught exception: " err origin)))

(defonce server (atom nil))

(defn start-server []
  (let [app   (express)
        new-port  (or env/PORT port)]
    (.use app (compression))
    (.use app (express/static "../frontend/public")) 
    (.use app "/" html/home-page) 
    (.use app html/home-page)
    (.listen app new-port "::" "0.0.0.0" 
             (fn [] (println "Port: " new-port)))))

  
(defn stop! []
    (when @server (.close @server)))


(defn start! []
  (println "Code updated.")
  (reset! server (start-server)))



