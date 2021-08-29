(ns proj0.handler
  (:require
   [ring.middleware.json :as json]
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [cheshire.core :as cheshire]
   [clojure.data.json :as to-json]
   [proj0.answers-reader :refer [survey-src upload-answers results-atom]]))

(def mount-target
  [:div#app
   [:div#load]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")
    [:script "proj0.core.init_BANG_()"]]))

(defn index-page
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(defn answers-handler [req]
  {:code 200
   :body (-> (:json-params req)
             (cheshire/generate-string {:escape-non-ascii true})
             (cheshire/parse-string true)
             (upload-answers))})

(defroutes handler
  (GET "/" [] index-page)
  (GET "/questions" [] (slurp survey-src))
  (POST "/answers" [] answers-handler)
  (GET "/results" [] (to-json/write-str @results-atom))
  (GET "/stats" [] index-page)
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> handler
      (json/wrap-json-response)
      (json/wrap-json-params)))
