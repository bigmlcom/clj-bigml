;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.model
  (:require (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a model given a dataset. Accepts the optional creation
   parameters defined in the BigML API docs:
      https://bigml.com/developers/models#m_create"
  [dataset & params]
  (let [params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params)
                      :dataset (api/location dataset))
        auth-params (select-keys params api/auth-params)]
    (api/create :model
                (:dev_mode params)
                {:content-type :json
                 :form-params form-params
                 :query-params auth-params})))

(defn list
  "Retrieves a list of models. Optional parameters are supported for
   pagination and filtering.  Details are available here:
      https://bigml.com/developers/models#m_list"
  [& params]
  (apply api/list :model params))

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
                     (api/convert-inputs model inputs)
                     inputs)
            result (root-fn inputs)]
        (if details result (:output result))))))
