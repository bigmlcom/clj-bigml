# clj-bigml

**clj-bigml** provides Clojure bindings for the
[BigML.io API](https://bigml.com/developers/overview).

## Installation

`clj-bigml` is available as a Maven artifact from
[Clojars](http://clojars.org/bigml/clj-bigml):

```clojure
[bigml/clj-bigml "0.1.0"]
```

## Overview

BigML offers a REST-style service for building, sharing, and
evaluating machine learning models.  Currently the only model variety
supported is [CART-style decision trees]
(http://en.wikipedia.org/wiki/Decision_tree_learning) grown in
an anytime, streaming fashion.  The trees use standard ML practices
such as information gain (or information gain ratio) for
classification problems, squared error for regression, and statistical
pruning options.

The service takes a white-box approach.  While predictions may be made
through the API, the models can also be downloaded for use locally
(either as BigML's native JSON format or as
[PMML](http://www.dmg.org/v4-1/GeneralStructure.html)).

The API also supports sampling and randomization options which enable
ensemble methods such as [random forests]
(http://en.wikipedia.org/wiki/Random_forest).

Please note, all code samples in this document assume that the
following namespaces are already required:

```clojure
(require '[bigml.api [core :as api]
                     [source :as source]
                     [dataset :as dataset]
                     [model :as model]
                     [prediction :as prediction]
                     [evaluation :as evaluation]])
```

## Authentication

To use this library you'll need an [account with BigML]
(https://bigml.com/accounts/register/), your user name, and your
[API key](https://bigml.com/account/apikey).

While new BigML accounts come with some free credits, you may avoid
spending your credits by enabling development mode.  Development mode
allows free access to the API but limits the size of the data you can
model (~1 MB limit).

There are three approaches to authentication when using the library.
The first, and perhaps easiest, is to set environment variables:

```console
export BIGML_USERNAME=johndoe
export BIGML_API_KEY=0123456789
export BIGML_DEV_MODE=true
```

If the environment variables are set, you may make calls to the client
without specifying the authentication information or the development
mode (this next code sample creates a data source, which will be
discussed later):

```clojure
(source/create "some_file.csv")
```

Alternatively, you can use the `with-connection` and `make-connection`:

```clojure
(api/with-connection (api/make-connection "johndoe" "0123456789" true)
  (source/create "some_file.csv"))
```

Finally, you can add the authentication information and the
development mode as parameters when calling client functions:

```clojure
(source/create "some_file.csv"
               :username "johndoe"
               :api_key "0123456789"
               :dev_mode true)
```

## Resources

There are five types of resources in the BigML API:
[sources](https://bigml.com/developers/sources),
[datasets](https://bigml.com/developers/datasets),
[models](https://bigml.com/developers/models),
[predictions](https://bigml.com/developers/predictions), and
[evaluations](https://bigml.com/developers/evaluations).  For more
about them, see the [tutorial videos on YouTube]
(http://www.youtube.com/playlist?list=PL16FC91153F8C47A7&feature=plcp).

`bigml.api.core` provides a set of functions that act as primitives
for all the resource types:

  - `list` - Returns a paginated list of the desired resource.
  - `create` - Creates a resource (although we recommended avoiding
               this and using the friendlier resource specific `create`
               fns).
  - `update` - Updates a resource (usually limited to textual descriptions
               like `name`).
  - `delete` - Deletes a resource.
  - `get` - Retrieves a resource.
  - `get-final` - Repeatedly attempts to `get` a resource until it is
                  finalized.

The other namespaces (`bigml.api.source`, `bigml.api.dataset`, …)
offer functions specific to that resource type.  At a minimum this
includes resource specific `list` and `create` functions which are
more convenient than the generic version.

### Sources

[Sources](https://bigml.com/developers/sources) represent the raw data
that you wish to analyze.

`bigml.api.source/create` makes it convenient to create sources.  It
supports three types of sources and will create the appropriate type
depending on the input to `create`.

**Local sources** are created from local files:

```clojure
(source/create "test/data/iris.csv.gz")
```

This will upload the file from your computer to BigML.  BigML supports
multiple formats such as CSVs (with space, tab, comma, or semicolon
separators), Excel spreadsheets, iWork Numbers, and Weka's ARFF files.

A variety of compression formats are also supported such as `.Z`
(Unix-compressed), `gz`, and `bz2`.

**Remote sources** are created from URLs:

```clojure
(source/create "https://static.bigml.com/csv/iris.csv")
```

BigML also supports [s3, azure, and odata URLs]
(http://blog.bigml.com/2012/12/07/bigmler-in-da-cloud-machine-learning-made-even-easier/).

**Inline sources** are created directly from Clojure data:

```clojure
(source/create [["Make" "Model" "Year" "Weight" "MPG"]
                ["AMC" "Gremlin" 1970 2648 21]
                ["AMC" "Matador" 1973 3672 14]
                ["AMC" "Gremlin" 1975 2914 20]
                ["Honda" "Civic" 1974 2489 24]
                ["Honda" "Civic" 1976 1795 33]])
```

Please note that inline sources support only small-ish amounts of data
(~5 MB limit).

### Datasets

[Datasets](https://bigml.com/developers/datasets) represent processed
data ready for modeling. They are created from sources and contain
statistical summarizations for each field (or column) in the data.
`bigml.api.dataset/create` makes dataset creation convenient.

In this example, from the well known [Iris data]
(http://en.wikipedia.org/wiki/Iris_flower_data_set) we create a
source, wait for completion, initiate a dataset, and wait for its
completion.

```clojure
(def iris-source
  (api/get-final (source/create "https://static.bigml.com/csv/iris.csv")))

(def iris-dataset
  (api/get-final (dataset/create iris-source)))
```

### Models

[Models](https://bigml.com/developers/models) are tree-based
predictive models built from datasets.

Continuing the Iris example, we now initialize a model and wait for it
to complete.  Since we don't specify an objective field or input
fields when building the model, it will default the objective as the
last field (in this case, "species").  The other fields become the
default inputs ("sepal length", "sepal width", "petal length" and
"petal width").

```clojure
(def iris-model
  (api/get-final (model/create iris-dataset)))
```

### Predictions

[Predictions](https://bigml.com/developers/predictions) may be
generated through the API.  Creating a prediction requires a model and
a set of inputs.  Prediction inputs can be formed two ways. They may
either be a map from field-id (assigned by the dataset when it's
created) to value, or it may be a list of input values that appear in
the same order as they did in the data source.

```clojure
(def iris-remote-prediction
  (prediction/create iris-model [7.6 3.0 6.6 2.1]))

(:prediction iris-remote-prediction)
;; --> {:000004 "Iris-virginica"}

;; Also valid:
;; (prediction/create iris-model {"000000" 7.6
;;                                "000001" 3.0
;;                                "000002" 6.6
;;                                "000003" 2.1})
```

Alternatively, we can use the model to create a local Clojure fn for
making predictions.

```clojure
(def iris-local-predictor
  (prediction/predictor iris-model))

(iris-local-predictor {"000000" 7.6
                       "000001" 3.0
                       "000002" 6.6
                       "000003" 2.1}) ;; --> "Iris-virginica"

(iris-local-predictor [7.6 3.0 6.6 2.1]) ;; --> "Iris-virginica"
```

The local prediction fn will also accept `:details` as an optional
parameter.  When true the fn will return extra information about the
prediction.  This includes the number of training instances that
reached this point in the tree, their objective field distribution,
and the
[confidence](https://bigml.com/developers/predictions#p_confidence) of
the prediction.

```clojure
(iris-local-predictor [7.6 3.0 6.6 2.1] :details true)
;; --> {:confidence 0.90819,
;;      :count 38,
;;      :objective_summary {:categories [["Iris-virginica" 38]]},
;;      :prediction {:000004 "Iris-virginica"}}
```

### Evaluations

Finally, an [evaluation](https://bigml.com/developers/evaluations) of
a model on a dataset may be generated through the API.

We continue the Iris example by evaluating our model on its own
training data.  This is poor form for a data scientist, but it will do
as a demonstration.

```clojure
(def iris-evaluation
  (api/get-final (evaluation/create iris-model iris-dataset)))

(:accuracy (:model (:result iris-evaluation)))
;; --> 1

(:confusion_matrix (:model (:result iris-evaluation)))
;; --> [[50 0 0] [0 50 0] [0 0 50]]
```

We have perfect accuracy and a spotless confusion matrix.  But of
course, never trust evaluations on training data.

If you've been following along in your REPL, you can clean up the
artifacts generated in these examples like so:

```clojure
(mapv api/delete [iris-source iris-dataset iris-model
                  iris-remote-prediction iris-evaluation])
```

## More Examples

See `test/bigml/test/api/examples.clj` for more examples of the API in
action.  We show how to break up a dataset into proper training and
testing sets through sampling options, and we show how to grow and
predict with a random forest.

## Support

Please report problems and bugs to our
[BigML.io issue tracker](https://github.com/bigmlcom/io/issues).

Discussions about language bindings take place in the general
[BigML mailing list](http://groups.google.com/group/bigml). Or join us
in our [Campfire chatroom](https://bigmlinc.campfirenow.com/f20a0).

## License

Copyright (C) 2012 BigML Inc.

Distributed under the Apache License, Version 2.0.
