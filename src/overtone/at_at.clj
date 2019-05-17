(ns overtone.at-at
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit ThreadPoolExecutor]
           [java.io Writer]))

(defrecord PoolInfo [thread-pool jobs-ref id-count-ref])
(defrecord MutablePool [pool-atom])
(defrecord RecurringJob [id created-at ms-period initial-delay job pool-info desc scheduled?])
(defrecord ScheduledJob [id created-at initial-delay job pool-info desc scheduled?])

(defn- format-date
  "Format date object as a string such as: 15:23:35s"
  [date]
  (.format (java.text.SimpleDateFormat. "EEE hh':'mm':'ss's'") date))

(defmethod print-method PoolInfo
  [obj ^Writer w]
  (.write w (str "#<PoolInfo: " (:thread-pool obj) " "
                 (count @(:jobs-ref obj)) " jobs>")))

(defmethod print-method MutablePool
  [obj ^Writer w]
  (.write w (str "#<MutablePool - "
                 "jobs: "(count @(:jobs-ref @(:pool-atom obj)))
                 ">")))

(defmethod print-method RecurringJob
  [obj ^Writer w]
  (.write w (str "#<RecurringJob id: " (:id obj)
                 ", created-at: " (format-date (:created-at obj))
                 ", ms-period: " (:ms-period obj)
                 ", initial-delay: " (:initial-delay obj)
                 ", desc: \"" (:desc obj) "\""
                 ", scheduled? " @(:scheduled? obj) ">")))

(defmethod print-method ScheduledJob
  [obj ^Writer w]
  (.write w (str "#<ScheduledJob id: " (:id obj)
                 ", created-at: " (format-date (:created-at obj))
                 ", initial-delay: " (:initial-delay obj)
                 ", desc: \"" (:desc obj) "\""
                 ", scheduled? " @(:scheduled? obj) ">")))

(defn- switch!
  "Sets the value of atom to new-val. Similar to reset! except returns the
  immediately previous value."
  [atom new-val]
  (let [old-val  @atom
        success? (compare-and-set! atom old-val new-val)]
    (if success?
      old-val
      (recur atom new-val))))

(defn- cpu-count
  "Returns the number of CPUs on this machine."
  []
  (.availableProcessors (Runtime/getRuntime)))



(defn- schedule-job
  "Schedule the fun to execute periodically in pool-info's pool with the
  specified initial-delay and ms-period. Returns a RecurringJob record."
  [pool-info fun initial-delay ms-period desc interspaced?]
  (let [initial-delay (long initial-delay)
        ms-period     (long ms-period)
        ^ScheduledThreadPoolExecutor t-pool (:thread-pool pool-info)
        job           (if interspaced?
                        (.scheduleWithFixedDelay t-pool
                                                 fun
                                                 initial-delay
                                                 ms-period
                                                 TimeUnit/MILLISECONDS)
                        (.scheduleAtFixedRate t-pool
                                              fun
                                              initial-delay
                                              ms-period
                                              TimeUnit/MILLISECONDS))
        start-time    (System/currentTimeMillis)
        jobs-ref      (:jobs-ref pool-info)
        id-count-ref  (:id-count-ref pool-info)]
    (dosync
     (let [id       (commute id-count-ref inc)
           job-info (RecurringJob. id
                                   start-time
                                   ms-period
                                   initial-delay
                                   job
                                   pool-info
                                   desc
                                   (atom true))]
       (commute jobs-ref assoc id job-info)
       job-info))))

(defn- wrap-fun-to-remove-itself
  [fun jobs-ref job-info-prom]
  (fn [& args]
    (let [job-info  @job-info-prom
          id        (:id job-info)
          sched-ref (:scheduled? job-info)]
      (reset! sched-ref false)
      (dosync
       (commute jobs-ref dissoc id))
      (apply fun args))))

(defn- schedule-at
  "Schedule the fun to execute once in the pool-info's pool after the
  specified initial-delay. Returns a ScheduledJob record."
  [pool-info fun initial-delay desc]
  (let [initial-delay (long initial-delay)
        ^ScheduledThreadPoolExecutor t-pool (:thread-pool pool-info)
        jobs-ref      (:jobs-ref pool-info)
        id-prom       (promise)
        ^Callable fun (wrap-fun-to-remove-itself fun jobs-ref id-prom)
        job           (.schedule t-pool fun initial-delay TimeUnit/MILLISECONDS)
        start-time    (System/currentTimeMillis)
        id-count-ref  (:id-count-ref pool-info)
        job-info      (dosync
                       (let [id       (commute id-count-ref inc)
                             job-info (ScheduledJob. id
                                                     start-time
                                                     initial-delay
                                                     job
                                                     pool-info
                                                     desc
                                                     (atom true))]
                         (commute jobs-ref assoc id job-info)
                         job-info))]
    (deliver id-prom job-info)
    job-info))

(defn- shutdown-pool-now!
  "Shut the pool down NOW!"
  [pool-info]
  (.shutdownNow (:thread-pool pool-info))
  (doseq [job (vals @(:jobs-ref pool-info))]
    (reset! (:scheduled? job) false)))

(defn- shutdown-pool-gracefully!
  "Shut the pool down gracefully - waits until all previously
  submitted jobs have completed"
  [pool-info]
  (.shutdown (:thread-pool pool-info))
  (let [jobs (vals @(:jobs-ref pool-info))]
    (future
      (loop [jobs jobs]
        (doseq [job jobs]
          (when (and @(:scheduled? job)
                     (or
                      (.isCancelled (:job job))
                      (.isDone (:job job))))
            (reset! (:scheduled? job) false)))

        (when-let [jobs (filter (fn [j] @(:scheduled? j)) jobs)]
          (Thread/sleep 500)
          (when (seq jobs)
            (recur jobs)))))))

(defn- mk-sched-thread-pool
  "Create a new scheduled thread pool containing num-threads threads."
  [num-threads]
  (let [t-pool (ScheduledThreadPoolExecutor. num-threads)]
    t-pool))

(defn- mk-pool-info
  [t-pool]
  (PoolInfo. t-pool (ref {}) (ref 0N)))

(defn mk-pool
  "Returns MutablePool record storing a mutable reference (atom) to a
  PoolInfo record which contains a newly created pool of threads to
  schedule new events for. Pool size defaults to the cpu count + 2."
  [& {:keys [cpu-count stop-delayed? stop-periodic?]
      :or {cpu-count (+ 2 (cpu-count))}}]
  (MutablePool. (atom (mk-pool-info (mk-sched-thread-pool cpu-count)))))

(defn every
  "Calls fun every ms-period, and takes an optional initial-delay for
  the first call in ms.  Returns a scheduled-fn which may be cancelled
  with cancel.

  Default options are
  {:initial-delay 0 :desc \"\"}"
  [ms-period fun pool & {:keys [initial-delay desc]
                         :or {initial-delay 0
                              desc ""}}]
  (schedule-job @(:pool-atom pool) fun initial-delay ms-period desc false))

(defn interspaced
  "Calls fun repeatedly with an interspacing of ms-period, i.e. the next
   call of fun will happen ms-period milliseconds after the completion
   of the previous call. Also takes an optional initial-delay for the
   first call in ms. Returns a scheduled-fn which may be cancelled with
   cancel.

   Default options are
   {:initial-delay 0 :desc \"\"}"
  [ms-period fun pool & {:keys [initial-delay desc]
                         :or {initial-delay 0
                              desc ""}}]
  (schedule-job @(:pool-atom pool) fun initial-delay ms-period desc true))

(defn now
  "Return the current time in ms"
  []
  (System/currentTimeMillis))

(defn at
  "Schedules fun to be executed at ms-time (in milliseconds).
  Use (now) to get the current time in ms.

  Example usage:
  (at (+ 1000 (now))
      #(println \"hello from the past\")
      pool
      :desc \"Message from the past\") ;=> prints 1s from now"
  [ms-time fun pool & {:keys [desc]
                       :or {desc ""}}]
  (let [initial-delay (- ms-time (now))
        pool-info  @(:pool-atom pool)]
    (schedule-at pool-info fun initial-delay desc)))

(defn after
  "Schedules fun to be executed after delay-ms (in
  milliseconds).

  Example usage:
  (after 1000
      #(println \"hello from the past\")
      pool
      :desc \"Message from the past\") ;=> prints 1s from now"
  [delay-ms fun pool & {:keys [desc]
                        :or {desc ""}}]
  (let [pool-info  @(:pool-atom pool)]
    (schedule-at pool-info fun delay-ms desc)))

(defn- shutdown-pool!
  [pool-info strategy]
  (case strategy
    :stop (shutdown-pool-gracefully! pool-info)
    :kill (shutdown-pool-now! pool-info)))

(defn stop-and-reset-pool!
  "Shuts down the threadpool of given MutablePool using the specified
  strategy (defaults to :stop). Shutdown happens asynchronously on a
  separate thread.  The pool is reset to a fresh new pool preserving
  the original size.  Returns the old pool-info.

  Strategies for stopping the old pool:
  :stop - allows all running and scheduled tasks to complete before
          waiting
  :kill - forcefully interrupts all running tasks and does not wait

  Example usage:
  (stop-and-reset-pool! pool)            ;=> pool is reset gracefully
  (stop-and-reset-pool! pool
                        :strategy :kill) ;=> pool is reset forcefully"
  [pool & {:keys [strategy]
           :or {strategy :stop}}]
  (when-not (some #{strategy} #{:stop :kill})
    (throw (Exception. (str "Error: unknown pool stopping strategy: " strategy ". Expecting one of :stop or :kill"))))
  (let [pool-atom      (:pool-atom pool)
        ^ThreadPoolExecutor tp-executor (:thread-pool @pool-atom)
        num-threads   (.getCorePoolSize tp-executor)
        new-t-pool    (mk-sched-thread-pool num-threads)
        new-pool-info (mk-pool-info new-t-pool)
        old-pool-info (switch! pool-atom new-pool-info)]
    (future (shutdown-pool! old-pool-info strategy))
    old-pool-info))

(defn- cancel-job
  "Cancel/stop scheduled fn if it hasn't already executed"
  [job-info cancel-immediately?]
  (if (:scheduled? job-info)
    (let [job       (:job job-info)
          id        (:id job-info)
          pool-info (:pool-info job-info)
          pool      (:thread-pool pool-info)
          jobs-ref  (:jobs-ref pool-info)]
      (.cancel job cancel-immediately?)
      (reset! (:scheduled? job-info) false)
      (dosync
       (let [job (get @jobs-ref id)]
         (commute jobs-ref dissoc id)
         (true? (and job (nil? (get @jobs-ref id))))))) ;;return true if success
    false))

(defn- cancel-job-id
  [id pool cancel-immediately?]
  (let [pool-info @(:pool-atom pool)
        jobs-info @(:jobs-ref pool-info)
        job-info (get jobs-info id)]
    (cancel-job job-info cancel-immediately?)))

(defn stop
  "Stop a recurring or scheduled job gracefully either using a
  corresponding record or unique id. If you specify an id, you also
  need to pass the associated pool."
  ([job] (cancel-job job false))
  ([id pool] (cancel-job-id id pool false)))

(defn kill
  "kill a recurring or scheduled job forcefully either using a
  corresponding record or unique id. If you specify an id, you also
  need to pass the associated pool."
  ([job] (cancel-job job true))
  ([id pool] (cancel-job-id id pool true)))

(defn scheduled-jobs
  "Returns a set of all current jobs (both scheduled and recurring)
  for the specified pool."
  [pool]
  (let [pool-atom (:pool-atom pool)
        jobs     @(:jobs-ref @pool-atom)
        jobs     (vals jobs)]
    jobs))

(defn- format-start-time
  [date]
  (if (< date (now))
    ""
    (str ", starts at: " (format-date date))))

(defn- recurring-job-string
  [job]
  (str "[" (:id job) "]"
       "[RECUR] created: " (format-date (:created-at job))
       (format-start-time (+ (:created-at job) (:initial-delay job)))
       ", period: " (:ms-period job) "ms"
       ",  desc: \""(:desc job) "\""))

(defn- scheduled-job-string
  [job]
  (str "[" (:id job) "]"
       "[SCHED] created: " (format-date (:created-at job))
       (format-start-time (+ (:created-at job) (:initial-delay job)))
       ", desc: \"" (:desc job) "\""))

(defn- job-string
  [job]
  (cond
    (= RecurringJob (type job)) (recurring-job-string job)
    (= ScheduledJob (type job)) (scheduled-job-string job)))

(defn show-schedule
  "Pretty print all of the pool's scheduled jobs"
  ([pool]
   (let [jobs (scheduled-jobs pool)]
     (if (empty? jobs)
       (println "No jobs are currently scheduled.")
       (dorun
        (map #(println (job-string %)) jobs))))))
