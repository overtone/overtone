(ns overtone.inst.io
  (:use [overtone.sc gens envelope]
        [overtone.studio mixer]))

(definst mic [amp 1]
  (let [src (in (num-output-buses:ir))]
    (* amp src)))
