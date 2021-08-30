(ns proj0.answers-reader
  (:require [cheshire.core :as cheshire]
            [clojure.set]
            [clojure.walk]))

(def survey-src 
  "https://gist.githubusercontent.com/kwamebig/103bed13417ed003a9c3cda2e164b96a/raw/796026d42ae121529e15c6dc075e6610312e2f11/survey.json")

(defn results-blank [survey]
  (clojure.set/rename-keys (assoc survey :questions
                                  (vec (for [i (:questions survey)]
                                         (assoc i :values (clojure.walk/keywordize-keys
                                                           (into {}
                                                                 (map #(hash-map % 0) (:values i))))))))
                           {:questions :results}))

(def results-atom (atom (-> survey-src
                            slurp
                            (cheshire/parse-string true)
                            (results-blank))))

(defn read-by-type [answer result]
  (case (:type result)
    "free-text" (assoc-in result [:values (keyword answer)]
                          (if-not ((keyword answer) (:values result)) 1
                                  (inc ((keyword answer) (:values result)))))
    "single-choice" (assoc result :values
                           (assoc (:values result) (keyword answer)
                                  (if-not ((keyword answer) (:values result)) 1
                                          (inc ((keyword answer) (:values result))))))
    "multiple-choice" (assoc result :values
                             (apply merge-with max
                                    (for [i (for [i answer]
                                              (update-in result [:values (keyword i)] inc))]
                                      (:values i))))))

(defn upload-answers [answers]
  (reset! results-atom
          (assoc @results-atom :results
                 (map (fn [answer result]
                        (if-not (:answer answer) result
                                (let [i (:answer answer)]
                                  (read-by-type i result))))
                      (:answers answers)
                      (:results @results-atom)))))
