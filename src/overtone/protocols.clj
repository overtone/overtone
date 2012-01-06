(ns overtone.protocols)

; This is a holding ground for working on protocols...

; One goal of defining protocols is to clearly delineate various groups of functionality
; so we can improve the composability of the Overtone building blocks.  Our current synths
; and instruments grew out of a desire to make things easy to use, but we need to refactor them
; split-up unneccessarily bound aspects of functionality, and then create new "easy" APIs using
; these refactored elements.

; Synths:
; * synth definition
; * filling of default parameter settings from atoms
; * mixed argument handling (in-order and/or keyword)
; * implements trigger, ctl and kill for single synth instances
; * places synth nodes in a default synth group

; Instruments:
; * allocates an output bus for all instances of the synth
; * appends the out ugen to automatically use the correct bus
; * allocates groups:
;  - inst container group
;  - instance group
;  - fx group
;  - mixer group
; * implements ctl and kill for all instances

; This bundling of functionality has led to two issues that demonstrate some of the problems:
; * Many synth designs are very general purpose, and with different settings they can sound like completely different instruments.  Currently the only way to use the same instrument multiple times in Overtone with different parameter settings is to copy/paste the definst and give it a new name.  Instead we need a way to build a "voice" given a synth design and a set of parameters.

; * Audio samples and the memory buffers they get loaded into can be single or multi-channel, but a synth design must have a fixed number of channels.  We need to define a protocol for triggering synths so we can implement higher level "meta-synths" that can actually use multiple underlying synths depending on the number of channels of the input buffer.  Many other kinds of meta-synths could exist that either generate synth definitions on the fly, or choose the most appropriate for the task.

; To begin lets try to isolate this functionality into simpler units:
; * synth definition
; * mixed argument handling
; * stored default parameters
; * trigger, control and kill
; * bus allocation and automatic append of out ugen
; * automatic instance placement
; * group allocation and fx/mixer setup


(defprotocol ISynthNode
  (trigger [this & params] "Instantiate the synth and start playing.")
  (control [this & params] "Modify control parameters of a synth.")
  (kill    [this] "Stop and delete a synth instance."))


