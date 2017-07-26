;; Copyright 2017 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.anomaly-score
  "Functions for scoring with BigML anomaly detectiors.
     https://bigml.com/api/anomalyscores"
  (:require (bigml.api [core :as api]
                       [utils :as utils]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates an anomaly score from an anomaly detector and input data. A
  detector can be represented through its id (e.g., `anomaly/123123`),
  or a map as returned by `get` or `list`.

  The inputs may either be a map (field ids to values), or a sequence
  of the inputs fields in the order they appeared during training.

  Accepts the optional creation parameters described in the BigML API docs:
     https://bigml.com/api/anomalyscores#as_anomaly_score_arguments

  HTTP response information is attached as meta data. Exceptions are thrown
  on failure unless :throw-exceptions is set to false (default is true),
  in which case the HTTP response details are returned as a map on failure."
  [anomaly-detector inputs & params]
  (utils/create :target :anomalyscore
                :origin [:anomaly anomaly-detector]
                :params params
                :inputs inputs))

(defn list
  "Retrieves a list of anomaly scores. The optional parameters can include
  pagination and filtering options detailed here:
     https://bigml.com/api/anomalyscores#as_listing_anomaly_scores

  Pagination details are returned as meta data attached to the list, along
  with the HTTP response information. Exceptions are thrown on failure unless
  :throw-exceptions is set to false (default is true), in which case the
  HTTP response details are returned in a map on failure."
  [& params]
  (apply api/list :anomalyscore params))

(defn- predicate-fn [{:keys [field op value] :as pred} id->index]
  (if (true? pred)
    (constantly true)
    (let [index (id->index field)
          in-set (when (#{"in" "in*"} op) (set value))
          op-fn (case op
                  "=" #(and % (= % value))
                  "=*" #(or (nil? %) (= % value))
                  "!=" #(not= % value)
                  ">" #(and % (> % value))
                  ">*" #(or (nil? %) (> % value))
                  ">=" #(and % (>= % value))
                  ">=*" #(or (nil? %) (>= % value))
                  "<" #(and % (< % value))
                  "<*" #(or (nil? %) (< % value))
                  "<=" #(and % (<= % value))
                  "<=*" #(or (nil? %) (<= % value))
                  "in" #(and % (boolean (in-set %)))
                  "in*" #(or (nil? %) (boolean (in-set %))))]
      (fn [inputs]
        (op-fn (inputs index))))))

(defn- predicates-fn [predicates id->index]
  (let [pred-fns (mapv #(predicate-fn % id->index) predicates)
        pred-count (count predicates)]
    (fn [inputs]
      (loop [i 0]
        (if (and (< i pred-count) ((pred-fns i) inputs))
          (recur (inc i))
          i)))))

(defn- H [i]
  (+ 0.5772156649 (Math/log i)))

(defn- c [n]
  (* 2 (- (H (dec n))
          (/ (dec n) n))))

(defn- estimate-depth [population]
  (if (> population 1)
    (max (c population) 1)
    0))

(defn- node-fn
  [{:keys [children population predicates]} id->index depth]
  (let [repeat-depth (estimate-depth population)
        children (mapv #(node-fn % id->index (inc depth)) children)
        children-count (count children)
        eval-preds (predicates-fn predicates id->index)
        pred-count (count predicates)]
    (fn [inputs normalize-repeats]
      (let [i (eval-preds inputs)]
        (cond (and (= i pred-count) (empty? children))
              (if normalize-repeats
                (+ depth repeat-depth)
                depth)
              (< 0 i pred-count) (dec depth)
              (and (= i pred-count) (seq children))
              (loop [i 0]
                (if (< i children-count)
                  (or ((children i) inputs normalize-repeats)
                      (recur (inc i)))
                  depth)))))))

(defn- scorer [{:keys [sample_size mean_depth]}]
  (let [cn (min (c sample_size) mean_depth)]
    #(Math/pow 2 (- (/ (double %) cn)))))

(defn detector
  "Returns the anomaly detector as a function for scoring locally.
   The returned function expects either a map of inputs (field ids to
   values), or a sequence of the input fields in the order they
   appeared during training.

   The generated fn will also accept two optional boolean parameter which
   toggles whether to adjust the anomaly scores to take into account
   repeated points. When true, points that are repeated in the training
   data will be considered less anomalous."
  [anomaly-detector]
  (let [{:keys [model input_fields]} (if (string? anomaly-detector)
                                       (api/get anomaly-detector)
                                       anomaly-detector)
        id->index (zipmap input_fields (range))
        tree-fns (mapv #(node-fn (:root %) id->index 1) (:trees model))
        score (scorer model)]
    (fn [inputs & [normalize-repeats]]
      (let [inputs (if (map? inputs)
                     (mapv inputs input_fields)
                     inputs)]
        (score (/ (reduce + (map (fn [tree-fn]
                                   (tree-fn inputs normalize-repeats))
                                 tree-fns))
                  (count tree-fns)))))))
