;; Copyright 2012, 2016, 2017 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.prediction
  "Offers functions specific for BigML predictions.
      https://bigml.com/developers/predictions"
  (:require (bigml.api [core :as api]
                       [utils :as utils]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a prediction given a model and the field inputs. The model
   may be either a string representing the model id (`model/123123`),
   or a map with either the full model (as returned with `get`) or a
   partial model (as returned with `list`).

   The inputs may either be a map (field ids to values), or a
   sequence of the inputs fields in the order they appeared during
   training.

   This function also accepts the optional creation parameters defined
   in the BigML API docs:
      https://bigml.com/developers/predictions#p_create

   HTTP response information is attached as meta data. Exceptions are
   thrown on failure unless :throw-exceptions is set as false (default
   is true), in which case the HTTP response details are returned as
   a map on failure."
  [model inputs & params]
  (utils/create :target :prediction
                :origin [:model model]
                :params params
                :inputs inputs))

(defn list
  "Retrieves a list of predictions. The optional parameters can include
   pagination and filtering options detailed here:
      https://bigml.com/developers/predictions#s_list

   Pagination details are returned as meta data attached to the list,
   along with the HTTP response information.  Exceptions are thrown on
   failure unless :throw-exceptions is set as false (default is true),
   in which case the HTTP response details are returned as a map on
   failure."
  [& params]
  (apply api/list :prediction params))

(defn output
  "Returns the prediction output."
  [prediction]
  (let [output-fn #(first (vals (:prediction %)))]
    (or (output-fn prediction)
        (output-fn (api/get-final prediction)))))

(def ^:private operator-fns
  {"=" = "!=" not= ">" > "<" < ">=" >= "<=" <=})

(defn- predicate-fn [predicate]
  (if (true? predicate)
    (fn [_] true)
    (let [{:keys [field operator value]} predicate
          pred-fn (operator-fns operator)]
      (fn [inputs]
        (when-let [input-val (inputs field)]
          (pred-fn input-val value))))))

(defn- node-fn [node]
  (when node
    (let [{:keys [predicate children]} node
          pred-fn (predicate-fn predicate)
          child-fns (map node-fn children)
          node (dissoc node :predicate :children)]
      (fn [inputs]
        (when (pred-fn inputs)
          (or (first (drop-while not (for [c child-fns] (c inputs))))
              node))))))

(def ^:private get-root (comp :root :model))

(defn predictor
  "Returns the model as a function for making predictions locally.
   The returned function expects either a map of inputs (field ids to
   values), or a sequence of the input fields in the order they
   appeared during training.

   The generated fn will also accept optional parameters:
     :details - When false, returns only the prediction output value.
                When true, returns a map including output, count,
                confidence, and the objective summary."
  [model]
  (let [model (if (get-root model) model (api/get model))
        root-fn (or (node-fn (get-root model))
                    (throw (Exception. "Invalid or unsupported model")))
        obj-field (keyword (first (:objective_fields model)))]
    (fn [inputs & {:keys [details by-name]}]
      (let [inputs (if (and (not (map? inputs)) (coll? inputs))
                     (utils/normalized-inputs model inputs by-name)
                     inputs)
            result (root-fn inputs)]
        (if details
          (dissoc (assoc result :prediction {obj-field (:output result)})
                  :output)
          (:output result))))))
