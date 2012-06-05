(ns ^{:doc "A custom datatype and helper functions for working with a lazy sequence of search results."
      :author "Kevin Neaton"}
  overtone.libs.freesound.search-results
  (:use [overtone.libs.freesound.url :only [build-url]]))

(deftype SearchResults [n-results results-seq]
  clojure.lang.Sequential
  clojure.lang.Seqable
  (seq [this] this)

  clojure.lang.Counted
  (count [_] n-results)

  clojure.lang.IPersistentCollection
  (cons [_ x]
    (SearchResults. n-results (.cons results-seq x)))
  (empty [_]
    (.empty results-seq))
  (equiv [_ o]
    (if (instance? SearchResults o)
      (and (= n-results (.n-results o))
           (.equiv results-seq (.results-seq o)))
      (.equiv results-seq o)))

  clojure.lang.ISeq
  (first [_]
    (.first results-seq))
  (next [_]
    (when (> n-results 1)
      (SearchResults. (dec n-results) (.next results-seq))))
  (more [_]
    (when (> n-results 1)
      (SearchResults. (dec n-results) (.more results-seq))))

  clojure.lang.IPending
  (isRealized [_] (.isRealized results-seq))

  clojure.lang.IDeref
  (deref [_] results-seq))

(defmethod print-method SearchResults
  [x writer]
  (.write writer (str x)))

(defn search-results
  "Create a new instance of SearchResults."
  [n-results results-seq]
  {:pre [(integer? n-results)
         (pos? n-results)
         (seq? results-seq)]}
  (SearchResults. n-results (lazy-seq results-seq)))

(defn next-fn
  "Returns a function that takes a freesound api response map and returns the
  'next' url with the given params or nil. This is needed to compensate for an
  api bug which causes some pagination uri's to be incomplete."
  [key params]
  (let [params (assoc params :api_key key)]
    (fn [resp]
      (when-let [next-url (:next resp)]
        (build-url next-url params)))))

(defn api-seq
  "Returns a lazy seq of paginated api responses. Retrieves the resource at
  'url' and passes the response to 'next-fn', which should return the next-url
  or nil."
  [url api-fn next-fn]
  (lazy-seq
   (let [resp (api-fn url)]
     (cons resp (when-let [next-url (next-fn resp)]
                  (api-seq next-url api-fn next-fn))))))

(defn lazy-cats
  "Returns a lazy seq over the concatenation the items in coll."
  [coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (concat (first s) (lazy-cats (rest s))))))
