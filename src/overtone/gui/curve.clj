(ns overtone.gui.curve)

; TODO: Write me!
; Use Scenegraph constructs to create a widget that can display and modify envelope 
; curve arrays.

; Envelope arrays are structured like this:
  ; * initial level
  ; * n-segments
  ; * release node (int or -99, tells envelope where to optionally stop until released)
  ; * loop node (int or -99, tells envelope which node to loop back to until released)
  ; [
  ;   - segment 1 endpoint level
  ;   - segment 1 duration
  ;   - segment shape
  ;   - segment curve
  ; ] * n-segments
(defn show-curve 
  "Display an envelope curve in the wave window."
  [c])
