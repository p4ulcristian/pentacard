(ns backend.env
  (:require ["node:process" :as process]))


(def DEVELOPER?              (-> process .-env .-DEVELOPER?))

(def PORT                    (-> process .-env .-PORT))

(def MONGO_URL               (-> process .-env .-MONGO_URL))

(def RAILWAY_PUBLIC_DOMAIN   (-> process .-env .-RAILWAY_PUBLIC_DOMAIN))

(def CLOUDFLARE_ACCOUNT_ID   (-> process .-env .-CLOUDFLARE_ACCOUNT_ID))

(def CLOUDFLARE_ACCOUNT_HASH (-> process .-env .-CLOUDFLARE_ACCOUNT_HASH))

(def CLOUDFLARE_API_TOKEN    (-> process .-env .-CLOUDFLARE_API_TOKEN))