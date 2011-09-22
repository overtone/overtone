(ns overtone.inst.io
  (:use [overtone.sc ugens envelope]
        [overtone.studio rig]))

(definst mic [amp 1]
  (let [src (in (num-output-buses:ir))]
    (* amp src)))
