(ns proj0.core
  (:require
   [reagent.core :as reagent]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [re-frame.core :as re-frame]
   [proj0.survey :refer [survey]]
   [proj0.stats :refer [bar-charts]]
   [proj0.state :as state]))

(def router
  (reitit/router
   [["/" :index]
    ["/stats" :stats]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

(defn page-for [route]
  (case route
    :index #'survey
    :stats #'bar-charts))

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header.links
         [:p [:a {:href (path-for :index)} "Survey"]]
         [:p [:a {:href (path-for :stats)} "Results"]]]
       [page]])))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [current-page] root-el)))

(defn init! []
  (re-frame/dispatch-sync [::state/initialize-db])
  (re-frame/dispatch-sync [::state/fetch-questions])
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)
        ))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
