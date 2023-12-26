(ns overtone.helpers.process-info
  "Inspect running processes"
  {:author "Arne Brasseur"}
  (:import
   (java.lang ProcessHandle)
   (java.util Optional)
   (java.util.stream Stream)))

(defn opt-val [^Optional o]
  (when (.isPresent o)
    (.get o)))

(defn stream-seq [^Stream s]
  (iterator-seq (.iterator s)))

(defn process-info [^ProcessHandle ph]
  (let [info (.info ph)]
    {:pid (.pid ph)
     :command (opt-val (.command info))
     :arguments (vec (opt-val (.arguments info)))
     :start-instant (opt-val (.startInstant info))
     :total-cpu-duration (opt-val (.totalCpuDuration info))
     :user (opt-val (.user info))}))

(defn ps
  "Equivalent of doing a `ps ax`, get info on running processes"
  []
  (map process-info (stream-seq (ProcessHandle/allProcesses))))
