;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.prediction
  "Offers functions specific for BigML predictions.
      https://bigml.com/developers/predictions"
  (:require (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn- convert-inputs [model inputs]
  (let [input-fields (or (:input_fields model)
                         (:input_fields (api/get-final model))
                         (throw (Exception. "Inaccessable model")))]
    (apply hash-map (flatten (map clojure.core/list input-fields inputs)))))

(defn create
  "Creates a prediction given a model and the field inputs.

   The inputs may either be a map (field ids to values), or a
   sequence of the inputs fields in the order they appeared during
   training.

   This function also accepts the optional creation parameters defined
   in the BigML API docs:
      https://bigml.com/developers/predictions#p_create"
  [model inputs & params]
  (let [inputs (if (and (not (map? inputs)) (coll? inputs))
                 (convert-inputs model inputs)
                 inputs)
        params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params)
                      :model (api/location model)
                      :input_data inputs)
        auth-params (select-keys params api/auth-params)]
    (api/create :prediction
                (:dev_mode params)
                {:content-type :json
                 :form-params form-params
                 :query-params auth-params})))

(defn list
  "Retrieves a list of predictions. The optional parameters can include
   pagination and filtering options detailed here:
      https://bigml.com/developers/predictions#s_list

   Pagination details are returned as meta information attached to the
   list."
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
                    (throw (Exception. "Invalid or unsupported model")))]
    (fn [inputs & {:keys [details]}]
      (let [inputs (if (and (not (map? inputs)) (coll? inputs))
                     (convert-inputs model inputs)
                     inputs)
            result (root-fn inputs)]
        (if details result (:output result))))))
