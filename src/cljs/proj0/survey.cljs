(ns proj0.survey
  (:require [re-frame.core :as re-frame]
            [proj0.state :as state]
            [clojure.string :as string]))

(defn render-text [{:keys [id]}]
  [:div.free-text
   [:input.free
    {:type "textarea" :name "free" :id id :placeholder "Text input"}]])

(defn render-radio [{:keys [id values]}]
  [:div.single-choice
   (for [value values]
     [:div.option {:key value}
      [:input.radio
       {:type "radio" :id (str id value) :name id :value value}]
      [:label.option-label
       {:for (str id value)}
       value]])])

(defn render-checkbox [{:keys [id values]}]
  [:div.multiple-choice
   (for [value values]
     [:div.option {:key value}
      [:input.checkbox
       {:type "checkbox" :id (str id value) :name id :value value}]
      [:label.option-label
       {:for (str id value)}
       value]])])
 
(defn question-template [{:keys [id type question required values]}]
  [:div.question-view
   [:h4 (+ id 1) ". " question (when required [:sup.alert " *Required"])]
   [:div.question-sub
    (case type
      "free-text" [render-text {:id id :question question}]
      "single-choice" [render-radio {:id id :question question :values values}]
      "multiple-choice" [render-checkbox {:id id :question question :values values}])]])

(defn read-text [id] (js/document.getElementById id))

(defn read-radio [id] (js/document.querySelector (str ".main input[name='" id "']:checked")))

(defn read-checkbox [id] (js/document.querySelectorAll (str ".main input[name='" id "']:checked")))


(defn read-answers [questions]
  (hash-map :answers
            (into []
                  (for [question questions
                        :let [id (:id question)]]
                    (hash-map :question (:question question)
                              :answer
                              (case (:type question)
                                "free-text" (when (seq (.-value (read-text id)))
                                              (.-value (read-text id)))
                                "single-choice" (when (read-radio id)
                                                  (.-value (read-radio id)))
                                "multiple-choice" (when (seq (read-checkbox id))
                                                    (into []
                                                          (for [value (read-checkbox id)]
                                                            (.-value value)))))
                              :required (:required question))))))



(defn required-test [answers]
  (every? false? (for [i (:answers answers)]
                   (if (and (:required i) (not (:answer i))) true false))))

(defn on-click [questions]
  (let [answers (read-answers questions)]
    (if (required-test answers) (re-frame/dispatch [::state/post-answers (clj->js answers)])
        (js/alert "Please answer all required questions!"))
    (prn answers)))

(defn survey []
  (fn []
    (let [survey @(re-frame/subscribe [::state/survey-data])]
      [:div.survey
       [:div.main
        [:title (:title survey)]
        [:h1.title (:title survey)]
        [:form
         (for [question (:questions survey)]
           [question-template
            {:key (:id question)
             :id (:id question)
             :type (:type question)
             :question (:question question)
             :required (:required question)
             :values (:values question)}])
         [:button.save
          {:on-click #(on-click (:questions survey))
           :type :reset}
          "Complete"]
         [:a.to-stats {:href (str state/host "/stats")
                       :class "button"} "See all results 📊"]]]])))


