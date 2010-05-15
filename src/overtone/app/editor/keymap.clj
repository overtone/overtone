(defn key-map [mode stroke handler]
  (let [km (get (:keymaps @editor*) mode)]
    (assoc km (key-stroke stroke)
           (text-action handler))))

; NOTE: property keys that start with ':' are turned into keywords...
(defn- wrap-entry [k v]
  (let [k (if (.startsWith k ":") (keyword (.substring k 1)) k)]
    (proxy [clojure.lang.IMapEntry] []
      (key [] k)
      (getKey [] k)
      (val [] v)
      (getValue [] v))))

(defn keymap-for [comp]
  (let [km (.getKeymap comp)]
    (proxy [clojure.lang.Associative clojure.lang.IFn] []
      (count [] (count (seq (.getBoundKeyStrokes km))))
      (seq   [] (map (fn [stroke] (wrap-entry stroke (.getAction km stroke)))
                     (seq (.getBoundKeyStrokes km))))
      (cons  [[k v]] (.addActionForKeyStroke km k v))
      (empty [] (keymap-for comp))
      (equiv [o] (= o km))
      (containsKey [k] (not (nil? (.getAction km k))))
      (entryAt     [k] (wrap-entry k (.getAction km k)))
      (assoc       [k v] (.addActionForKeyStroke km k v))
      (valAt
        ([k] (if (= k :keymap)
               km
               (.getAction km k)))
        ([k d] (if-let [a (.getAction km k)]
                 a
                 (do
                   (.addActionForKeyStroke km k d)
                   d)))))))
;      (invoke ([key] (manager/do #(get-property obj key)))
;              ([key default] (manager/do #(if (has-property? obj key)
;                                            (get-property obj key)
;                                            default)))))))

(defn insert-mode-map [comp]
  (let [insert-mode (keymap-for comp)
        on (fn [stroke handler] (assoc insert-mode
                                       (key-stroke stroke)
                                       (text-action handler)))]
    (on "control N" file-new)
    (on "control O" file-open)
    (on "control S" file-save)
    (on "control A" select-all)
    (on "control C" text-copy)
    (on "control V" text-paste)
    (on "control X" text-cut)
    (on "control typed +" font-grow)
    (on "control typed =" font-grow)
    (on "control typed -" font-shrink)

    (on "control E"
          #(event :overtone.gui.repl/repl-write
                  :text (.trim (.getSelectedText (:editor @editor*)))))))

