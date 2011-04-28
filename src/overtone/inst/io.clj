(ns overtone.inst.io
  (:use overtone.core))

(definst mic [amp 1]
  (let [src (in (num-output-buses:ir))]
    (* amp src)))

