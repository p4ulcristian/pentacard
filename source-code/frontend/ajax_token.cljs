(ns frontend.ajax-token
  (:require [ajax.core :refer [default-interceptors to-interceptor]]))


(defn remove-interceptor [interceptor-name]
  (vec
   (filter (fn [a] (not= interceptor-name (:name a)))
           @default-interceptors)))

(defn remove-token-from-requests []
  (reset! default-interceptors (remove-interceptor "privy-token-interceptor")))


(defn add-token-to-requests [token]
  (let [token-interceptor (to-interceptor {:name "privy-token-interceptor"
                                           :request #(assoc-in % [:headers "authorization"] token)})]
    (swap! default-interceptors (partial cons token-interceptor))))


