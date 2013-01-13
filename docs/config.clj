;; Example Overtone Config.
;; ========================
;;
;; This lives in ~/.overtone/config.clj
;;
;; This example shouldn't be perceived as being typical - it's a
;; contrived example which uses all the possible keys (typically with
;; their default values) in an attempt to introduce their existence
;; and explain their meaning and usage.

{
 :server :internal, ; use the internal server by default. This option is
                    ; consulted when you use overtone.live. You may also
                    ; specify :external to boot an external server as
                    ; default.

 :user-name "Sam",  ; This is the name that Overtone will use to refer
                    ; to you. For example, you see this being used in
                    ; the boot message.

 :log-level :warn   ; The log level (default is :warn). Options are:
                    ; :error - only logs errors and exceptions
                    ; :warn  - logs :error messages and warnings
                    ; :info  - logs :error, :warn messages and
                    ;          operational info
                    ; :debug - logs :error, :warn, :info and
                    ;          diagnostic information

 :use-mmj false     ; Effective only on OS X systems.
                    ; If set to true, will use mmj MIDI objects in
                    ; preference to the default JVM MIDI implementation.
                    ; If set to false (the default), will always ignore
                    ; mmj objects. For more information regarding mmj:
                    ; http://www.humatic.de/htools/mmj.htm

 :sc-args           ; Argument map used to boot the SuperCollider server
                    ; with the following arguments:
 {
  :load-sdefs? 1                ; Load synthdefs on boot? 0 or 1

  :nrt-output-sample-format nil ; Sample format for non-realtime mode

  :block-size 64                ; Block size

  :rendezvous? 0                ; Publish to rendezvous? 0 or 1

  :ugens-paths nil              ; A list of paths of ugen directories. If
                                ; specified, the standard paths are NOT
                                ; searched for plugins.

  :verbosity 0                  ; Verbosity mode. 0 is normal behaviour,
                                ; -1 suppress information messages, -2
                                ; suppresses informational and many error
                                ; messages

  :nrt-output-filename nil      ; Output filename for non-realtime mode

  :max-audio-bus 512            ; Number of audio bus channels

  :nrt-cmd-filename nil         ; Command filename for non-realtime mode

  :realtime? 1                  ; Run in realtime mode? If 0 then the
                                ; other nrt flags must be set

  :max-w-buffers 64             ; Number of wire buffers

  :max-nodes 1024               ; Max number of executing nodes allowed in the server

  :nrt-input-filename nil       ; Input filename for non-realtime mode

  :max-output-bus 8             ; Number of output bus channels

  :hw-buffer-size nil           ; Hardware buffer size

  :pwd nil                      ; When using TCP, the session password
                                ; must be the first command sent.

  :max-logins 64                ; Maximum number of named return
                                ; addresses stored - also maximum number
                                ; of tcp connections accepted.

  :hw-device-name nil           ; Hardware device name

  :max-sdefs 1024               ; Max number of synthdefs allowed

  :hw-sample-rate nil           ; Hardware sample rate

  :port 57711                   ; Port number

  :restricted-path nil          ; Prevents file-accesing OSC commands
                                ; from accessing files outside the
                                ; specified path.

  :max-input-bus 8              ; Number of input bus channels

  :nrt-output-header-format nil ; Header format for non-realtime mode

  :udp? 1                       ; 1 means use UDP, 0 means use TCP

  :num-rand-seeds 64            ; Number of random seeds

  :in-streams nil               ; Input streams enabled

  :max-buffers 1024             ; Number of sample buffers

  :out-streams nil              ; Output streams enabled

  :max-control-bus 4096         ; Number of control bus channels

  :rt-mem-size 262144           ; Real time memory size
  }}
