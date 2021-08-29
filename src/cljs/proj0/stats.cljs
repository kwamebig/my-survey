(ns proj0.stats
  (:require [re-frame.core :as re-frame]
            [proj0.state :as state])) 

;; -------------------------
;; Make bar charts

(defn show-percent [parent-width percent]
  [:div.percent-head
   {:style
    {:width parent-width
     :height "1.9em"}}
   [:div.percent-sub
    {:style
     {:width percent
      :height "100%"
      :background-color "green"
      :font-size "4pt"}
     :dangerouslySetInnerHTML
     {:__html "&nbsp;"}}]])

(defn show-bar-chart [{:keys [values]}]
  [:div.pct
   [:table
    [:tbody
     (map
      (fn [key]
        (let [percent (-> (get values key) (str "%"))]
          ^{:key key}
          [:tr.tbl
           [:td key (show-percent "18em" percent)]
           [:td percent]]))
      (keys values))]]])

(defn build-item
  [item {:keys [question type values] :as all}]
  ^{:key item}
  [:div.container
   [:div.row
    [:h4
     [:span question]]]
   [:div.row
    (show-bar-chart all)]])

;; -------------------------
;; Transform answers to percents

(defn percent [p t] (/ (* p 100.0) t))
(defn round [x] (int (Math/round x)))
(def get-percentage-round (comp round percent))

(defn form-percents [m]
  (zipmap (keys m)
          (for [i (vals m)]
            (get-percentage-round i (apply + (vals m))))))

(defn answers->percents [m]
  (assoc m :results (map #(assoc % :values (form-percents (get % :values)))
                         (get m :results))))

;; -------------------------
;; Make TOP 5 answers and rest

(defn but-top5 [m] (->> m
                        (sort-by second #(compare %2 %1))
                        (drop 5)))

(defn append-rest [coll]
  (when-not (zero? (reduce + 0 (vals coll)))
    (first (conj coll [:*Rest (reduce + 0 (vals coll))]))))

(defn with-rest [m] (into {} (conj (->> m
                                        (sort-by second #(compare %2 %1))
                                        (take 5))
                                   (append-rest (but-top5 m)))))

(defn remove-zeros [m]
  (into {} (remove (comp zero? val) m)))

(defn top5-and-rest [m]
  (assoc m :results
         (map #(assoc % :values
                      (->> (get % :values)
                           (with-rest)))
              (get m :results))))

;; -------------------------
;; Render bar charts

(defn bar-charts []
  (re-frame/dispatch [::state/fetch-results])
  (fn []
    (let [results @(re-frame/subscribe [::state/survey-results])]
      [:div.survey
       [:div.main
        [:h1.title "Results of " (:title results)]
        (map-indexed build-item
                     (:results (->> results
                                    (answers->percents)
                                    (top5-and-rest))))
        [:a.to-survey {:href state/host
                       :class "button"} "Go back to survey 📝"]]])))

