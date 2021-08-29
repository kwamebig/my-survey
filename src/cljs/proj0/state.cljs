(ns proj0.state
  (:require
   [re-frame.core :as re-frame]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]))

(def host "http://localhost:3000")

(def default-db
  {:survey-data {:title ""
                 :questions []}
   :survey-results {:title ""
                    :results []}})

(def debug?
  ^boolean goog.DEBUG)

(re-frame/reg-sub
 ::survey-data
 (fn [db]
   (get db :survey-data)))

(re-frame/reg-sub
 ::survey-results
 (fn [db]
   (get db :survey-results)))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   default-db))

(re-frame/reg-event-fx
 ::fetch-questions
 (fn [{:keys [db] :as all} _]
   {:http-xhrio {:uri (str host "/questions")
                 :method :get
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:fetch-questions-ok]
                 :on-failure [:fetch-error]}}))

(defn append-ids [m]
  (assoc m :questions 
         (vec (map-indexed #(assoc %2 :id %1)
                           (get m :questions)))))

(re-frame/reg-event-db
  :fetch-questions-ok
  (fn [db [_ response]]
    (assoc db :survey-data (append-ids (js->clj response)))))

(re-frame/reg-event-fx
 ::post-answers
 (fn [_world [_ data]]
   {:http-xhrio {:uri (str host "/answers")
                 :method :post
                 :params data
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:post-answers-ok]
                 :on-failure [:post-answers-no]}}))

(re-frame/reg-event-db
  :post-answers-ok
  (fn [_ [_ response]]
    (println "200 OK" response)))

(re-frame/reg-event-db
  :post-answers-no
  (fn [_ [_ response]]
    (println "Error " response)))

(re-frame/reg-event-fx
 ::fetch-results
 (fn [{:keys [db] :as all} _]
   {:http-xhrio {:uri (str host "/results")
                 :method :get
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:fetch-results-ok]
                 :on-failure [:fetch-error]}}))


(re-frame/reg-event-db
  :fetch-results-ok
  (fn [db [_ response]]
    (assoc db :survey-results (js->clj response))))

(re-frame/reg-event-db
  :fetch-error
  (fn [_ [_ response]]
    (println "Error " response)))


