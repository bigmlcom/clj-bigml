;; Copyright 2017 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.script
  (:require (bigml.api [core :as api]
                       [script :as script]))
  (:use clojure.test))

(defn- create-and-test [artifact & {:as params}]
  (let [sticky-pars (flatten (seq (select-keys
                                   params
                                   [:username :api_key :dev_mode])))
        params (flatten (seq params))
        initial (apply api/get-final
                       (apply script/create artifact params) sticky-pars)
        script-name (str "test-script" (rand-int 1000))
        updated (apply api/update initial {:name script-name} sticky-pars)
        retrieved (apply api/get updated sticky-pars)
        deleted (apply api/delete retrieved sticky-pars)]
    (is (and initial updated retrieved))
    (is (true? deleted))
    (is (thrown? Exception (api/get initial)))
    (is (= script-name (:name updated) (:name retrieved)))))

(deftest script-create
  (testing "Script creation from string"
    (create-and-test "(define n 1)"))
  (testing "Script creation from stream"
    (create-and-test (java.io.FileInputStream. "test/data/script.whizzml"))))
