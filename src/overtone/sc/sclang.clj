(ns overtone.sc.sclang
  (:require
   [babashka.process :as proc]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [overtone.helpers.system :refer [get-os linux-os? mac-os? windows-os?]]
   [overtone.sc.machinery.server.connection :as ov.conn]
   [overtone.sc.synth :as ov.synth]))

(defonce *sclang-user-path (atom nil))

(defn sclang-path
  "The user must have SuperCollider installed.

  For sclang location in each OS, see
  https://github.com/supercollider/supercollider/wiki/Path-searching#scide-finds-sclang"
  []
  (or @*sclang-user-path
      (cond
        (mac-os?)
        "/Applications/SuperCollider.app/Contents/MacOS/sclang"
        ;; It's simply `sclang` in non OSX environments,
        ;; https://github.com/supercollider/supercollider/wiki/Path-searching#scide-finds-sclang
        ;; TODO Someone should test on Linux and Windows to confirm that this works.
        (windows-os?)
        "sclang.exe"

        :else
        "sclang")))

(defn transpile
  "Converts hiccup-like syntax to a SC string."
  [sc-clj]
  (let [[op & body] (if (sequential? sc-clj)
                      sc-clj
                      [sc-clj])]
    (try
      (cond
        (and (= op :SynthDef) (sequential? sc-clj))
        (let [{:keys [args resources-dir]
               :or {resources-dir "resources"}
               synthdef-name :name}
              (first body)

              synthdef-name (-> synthdef-name
                                (str/replace #"\." "_")
                                (str/replace #"/" "_"))

              resource-prefix "sc/synthdef"
              file-dir (str resources-dir "/" resource-prefix)

              file-path (str file-dir "/" synthdef-name ".scsyndef")
              resource-path (str resource-prefix "/" synthdef-name ".scsyndef")

              metadata-file-path (str file-dir "/" synthdef-name ".edn")
              metadata-resource-path (str resource-prefix "/" synthdef-name ".edn")

              body (rest body)
              synthdef-str (str "SynthDef"
                                "(" (str "\"" synthdef-name "\"" ",") " {\n"
                                (->> [(str "arg "
                                           (->> args
                                                (mapv (fn [[arg-identifier default]]
                                                        (str (name arg-identifier) "=" default)))
                                                (str/join ", "))
                                           ";")

                                      (->> body
                                           (mapv (fn [row]
                                                   (str (transpile row) ";")))
                                           (str/join "\n  "))]
                                     (mapv #(str "  " %))
                                     (str/join "\n"))
                                (format "\n}).writeDefFile(\"%s\").add;"
                                        file-dir))]
          {:sc-clj sc-clj
           :synthdef-name synthdef-name
           :file-path file-path
           :resource-path resource-path
           :metadata-file-path metadata-file-path
           :metadata-resource-path metadata-resource-path
           :synthdef-str synthdef-str
           ;; This one is to ease debugging.
           :synthdef-str-vec (str/split-lines synthdef-str)})

        (contains? #{:* :=} op)
        (->> body
             (mapv transpile)
             (str/join (str " " (name op) " ")))

        (contains? #{:.} op)
        (->> body
             (mapv transpile)
             (str/join (name op)))

        (= :vars op)
        (str "var "
             (->> body
                  (mapv (fn [var-identifier]
                          (name var-identifier)))
                  (str/join ", ")))

        (= :raw op)
        (->> body
             (str/join "; "))

        (and (keyword? op) body)
        (str (name op)
             "( "
             (->> body
                  (mapv transpile)
                  (str/join ", "))
             " )")

        (and (or (keyword? op)
                 (symbol? op))
             (not body))
        (name op)

        (sequential? sc-clj)
        (str "["
             (->> sc-clj
                  (str/join ", "))
             "]")

        (number? sc-clj)
        sc-clj

        (string? sc-clj)
        (str "\"" sc-clj "\"")

        (map? sc-clj)
        (->> sc-clj
             (mapv (fn [[k v]]
                     (str (name k) ": " v)))
             (str/join ", "))

        :else
        (throw (ex-info "Unhandled expression while transpiling into SC"
                        {:op op
                         :body body})))
      (catch Exception e
        (throw (ex-info "Transpiler error"
                        {:op op
                         :body body}
                        e))))))
#_(-> [:SynthDef {:name `event
                  :args [[:freq 240] [:amp 0.5] [:pan 0.0]]}
       [:vars :env]
       [:= :env [:EnvGen.ar
                 [:Env [0 1 1 0] [0.01 0.1 0.2]]
                 {:doneAction 2}]]
       [:Out.ar 0 [:Pan2.ar [:* [:Blip.ar :freq] :env :amp]
                   :pan]]]
      transpile)

(defonce ^:private *procs
  (atom []))

(defn stop-procs!
  "Stop all running sclang process."
  []
  (mapv proc/destroy @*procs)
  (reset! *procs []))
#_(stop-procs!)

(defn -wrap-code-with-interpreter
  ([code-str]
   (-wrap-code-with-interpreter code-str {}))
  ([code-str {:keys [boot]
              :or {boot false}}]
   (let [boot-init-coll ["s.boot;"
                         "s.waitForBoot({"]
         boot-end-coll ["});"]

         app-clock-init-coll ["AppClock.sched(0.0,{ arg time;"]
         app-clock-end-coll ["});"]]
     #_(println code-str)
     (format (->> (concat
                   (when boot boot-init-coll)
                   app-clock-init-coll
                   [""
                    "try {this.interpret("
                    "%s"
                    ") }"
                    "  { |error|"
                    "    \""
                    ""
                    "ERROR"
                    "----------------------------------"
                    "\".postln;"
                    "    error.reportError;"
                    "    \"-------------------------------------"
                    ""
                    "\".postln;"
                    "  };"
                    ""]
                   app-clock-end-coll
                   (when boot boot-end-coll))
                  (str/join "\n"))
             (pr-str code-str)))))

(defn exec!
  "Execute a sclang script in the background.

  It returns a `babashka.process/process`, `out` and `err` are redirected
  to stdout.

  Call `stop-procs!` or `babaskha.process/destroy-tree` to kill the
  returned process.

  E.g.

    (exec! [:. :SynthDef :help])"
  [sc-clj]
  (stop-procs!)
  (let [temp-scd (io/file (str ".overtone/sc/" "_" (random-uuid) ".scd"))
        _port (or (:port @ov.conn/connection-info*)
                  (:port (:opts @ov.conn/connection-info*)))
        str (-wrap-code-with-interpreter (transpile sc-clj))]
    (io/make-parents temp-scd)
    (spit temp-scd str)
    (let [proc (proc/process {:out *out* :err *err*} (sclang-path) temp-scd)]
      (swap! *procs conj proc)
      proc)))
#_(exec! [:. :SynthDef :help])
;; If you don't have `FoaRotate` installed, you should see an error in the REPL output.
#_(exec! [:. :FoaRotate :help])

(defn help
  "Open SC help window for a object."
  [obj-k]
  (exec! [:. obj-k :help]))
#_(help :SynthDef)

(defn- check-proc!
  [proc args]
  (loop [counter 15]
    (cond
      (zero? counter)
      (throw (ex-info (str "Process had an error, check the stdout, also check "
                           "that you have SuperCollider installed, https://supercollider.github.io/downloads")
                      (merge args {:proc (proc/destroy-tree proc)})))

      (proc/alive? proc)
      (do (Thread/sleep 200)
          (recur (dec counter)))

      :else
      @proc)))

(defn -synthdef-save!
  "This is an internal function, read the comments in the code to understand
  what it's doing."
  [{:keys [synthdef-name file-path resource-path
           metadata-file-path metadata-resource-path
           synthdef-str sc-clj]
    :as args}]
  (if (and (io/resource resource-path)
           (io/resource metadata-resource-path)
           (= sc-clj (-> (io/resource metadata-resource-path)
                         slurp
                         edn/read-string
                         :sc-clj)))
    ;; Use the resource directly if the code is the same.
    (io/resource resource-path)
    ;; Code is not cached, let's run sclang.
    (let [temp-scd (io/file (str ".overtone/sc/" synthdef-name ".scd"))]

      ;; Make parent folders in the saved path (if necessary).
      (io/make-parents temp-scd)
      (io/make-parents file-path)

      ;; Write temp SCD file.
      (spit temp-scd (-> synthdef-str
                         (str "\n\n0.exit;")
                         -wrap-code-with-interpreter))

      ;; Stop existing running processes (if any).
      (stop-procs!)
      ;; Run process.
      (check-proc! (proc/process {:out *out* :err :out} (sclang-path) temp-scd) args)

      (when-not (io/resource resource-path)
        (throw (ex-info (str "Error when defining a synthdef, resource not found.\nCheck the stdout, also check "
                             "that your `resources` folder is a `resource` to this Java application.\nAt last, "
                             "check that SuperCollider is installed, https://supercollider.github.io/downloads")
                        args)))

      ;; Also, save a metadata file for the generated .scsyndef one so we
      ;; can use it for caching. Also, people who don't have sclang available
      ;; on their computers will be able to load the synthdef anyway as it's
      ;; a normal file (assuming the ugens for external plugins are loaded ofc).
      (spit metadata-file-path (binding [*print-length* nil
                                         *print-level* nil]
                                 (pr-str {:sc-clj sc-clj})))

      (io/resource resource-path))))

(comment

  (def my-synth
    (-> [:SynthDef {:name `event-2
                    :args [[:freq 240] [:amp 0.5] [:pan 0.0]]}
         [:vars :env]
         [:= :env [:EnvGen.ar
                   [:Env [0 1 1 0] [0.01 0.1 0.2]]
                   {:doneAction 2}]]
         [:Out.ar 0 [:Pan2.ar [:* [:Blip.ar :freq] :env :amp]
                     :pan]]]
        transpile
        -synthdef-save!
        ov.synth/synth-load))

  (my-synth :freq 100)

  ())

(defn synth
  "Defines a synth (SynthDef in SC) from a clojure data representation."
  [sc-clj]
  (-> sc-clj
      transpile
      -synthdef-save!
      ov.synth/synth-load))

(defn SynthDef
  [opts & body]
  (into [:SynthDef opts]
        body))

(comment

  (def my-synth
    (synth
     (SynthDef
      {:name 'event
       :args [[:freq 120] [:amp 0.5] [:pan 0.0]]}
      [:vars :env]
      [:= :env [:EnvGen.ar
                [:Env [0 1 1 0] [0.01 0.1 0.2]]
                {:doneAction 2}]]
      [:Out.ar 0 [:Pan2.ar [:* [:Blip.ar :freq] :env :amp]
                  :pan]])))

  (my-synth)

  ())

(defmacro defsynth
  "Defines a synth (SynthDef in SC) from a clojure data representation.

  This puts a metadata file and a .scsyndef file in a location derived from `s-name`
  inside `resources-dir`.

  `opts-map` can have the following optional keys
    - `:args`, this is already derived from `params`, so you don't need to set it
    - `:resources-dir`, default to \"resources\", it should be a resource folder

  This will call your SuperColliders's `sclang` command at the first time while using `sc-clj`
  for caching. As long you distribute the generated .scsyndef file in the right location and you don't
  modify `sc-clj`, the final user won't need to have `sclang` or SuperCollider installed
  on their machines.

  Check `sclang-path` code for the default locations. You can also set the
  `*sclang-user-path` atom to use an arbitrary `sclang` location.

  --------------------
  ;; Example.
  ;; When you evaluate the form below, you should see new files in
  ;; `resources/sc/synthdef`.
  (sclang/defsynth my-synth
    \"Some synth.\"
    [freq 440, amp 0.5, pan 0.0]
    [:vars :env]
    [:= :env [:EnvGen.ar [:Env [0 1 1 0] [0.01 0.1 0.2]] {:doneAction 2}]]
    [:Out.ar 0 [:Pan2.ar [:* [:Blip.ar :freq] :env :amp]
                :pan]])

  ;; Run it
  (my-synth :freq 220)"
  [s-name & s-form]
  {:arglists '([name doc-string? opts-map? params sc-clj])}
  (let [[doc-string opts-map params sc-clj] (cond
                                              (string? (first s-form))
                                              (if (map? (second s-form))
                                                [(first s-form) (second s-form) (nth s-form 2) (drop 3 s-form)]
                                                [(first s-form) nil (nth s-form 1) (drop 2 s-form)])

                                              (map? (first s-form))
                                              [nil (first s-form) (nth s-form 1) (drop 2 s-form)]

                                              :else
                                              [nil nil (first s-form) (drop 1 s-form)])]
    `(do (def ~s-name
           (synth
            (SynthDef
             (merge {:name (symbol (str *ns*) ~(str s-name))
                     :args ~(->> params
                                 (partition-all 2 2)
                                 (mapv (fn [[arg default]]
                                         [(keyword arg) default])))}
                    ~opts-map)
             ~@sc-clj)))
         (alter-meta! (var ~s-name) merge (cond-> (meta ~s-name)
                                            ~doc-string (assoc :doc ~doc-string)))
         (var ~s-name))))

(comment

  (defsynth my-synth-3
    "Some synth."
    [freq 440, amp 0.5, pan 0.0]
    [:vars :env]
    [:= :env [:EnvGen.ar [:Env [0 1 1 0] [0.01 0.1 0.2]] {:doneAction 2}]]
    [:Out.ar 0 [:Pan2.ar [:* [:Blip.ar :freq] :env :amp]
                :pan]])

  (my-synth-3)

  ())
