(ns overtone.gui
  (:use [overtone.helpers ns])
  (:require [overtone.gui control
             info mixer transport
             stepinator sequencer pianoroll
             surface wavetable]))

(immigrate
  'overtone.gui.scope
  'overtone.gui.control
  'overtone.gui.info
  'overtone.gui.mixer
  'overtone.gui.transport
  'overtone.gui.stepinator
  'overtone.gui.sequencer
  'overtone.gui.pianoroll
  'overtone.gui.surface
  'overtone.gui.wavetable)

