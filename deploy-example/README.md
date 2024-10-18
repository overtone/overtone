# Deploying Overtone

## Uberjar with AOT compiled sources

An example script `./script/build` shows how to generate an uberjar
that executes the main class `src/overtone_deploy/main.clj`.

```
$ pwd
/Users/overtone
$ ./script/build
+ mkdir -p classes target
+ rm -r classes/casa classes/clojure classes/org classes/overtone classes/overtone_deploy target/deploy-example.jar
+ clojure -M -e '(do (compile '\''overtone-deploy.main) (System/exit 0))'
--> Compiling Overtone...
--> (use 'overtone.live :reload) or restart JVM to use SuperCollider after compilation
--> Initiating shutdown
+ clj -Sdeps '{:aliases {:uberjar {:replace-deps {uberdeps/uberdeps {:mvn/version "1.4.0"}} :replace-paths []}}}' -M:uberjar -m uberdeps.uberjar --main-class overtone-deploy.main
[uberdeps] Packaging target/deploy-example.jar...
+ src/**
+ classes/**
+ org.clojure/clojure #:mvn{:version "1.12.0"}
.   org.clojure/core.specs.alpha #:mvn{:version "0.4.74"}
.   org.clojure/spec.alpha #:mvn{:version "0.5.238"}
+ overtone/overtone #:local{:root "/Users/overtone"}
.   casa.squid/jack #:mvn{:version "0.2.12"}
.     org.jaudiolibs/jnajack #:mvn{:version "1.4.0"}
.       net.java.dev.jna/jna #:mvn{:version "5.15.0"}
.   clj-glob/clj-glob #:mvn{:version "1.0.0"}
.   commons-net/commons-net #:mvn{:version "3.10.0"}
.   javax.jmdns/jmdns #:mvn{:version "3.4.1"}
.   org.clojure/data.json #:mvn{:version "2.5.0"}
.   overtone/at-at #:mvn{:version "1.3.58"}
[uberdeps] Packaged target/deploy-example.jar in 3391 ms
```

`System/exit` is used to terminate the JVM after AOT compilation of Overtone,
which seems to hang otherwise.

## macOS

Overtone has two dependencies: `java` and `scsynth`.

These are defined in `bin`. You should redefine them to
point to real paths on your computer/deployment.

```
$ ./script/run
+ PATH=bin
+ java -Dovertone.home-dir=config -jar target/deploy-example.jar
--> Loading Overtone...
[overtone.live] [INFO] Found SuperCollider server: bin/scsynth (PATH)
--> Booting external SuperCollider server...
[overtone.live] [INFO] Booting SuperCollider server (scsynth) with cmd: bin/scsynth -u 39720 -b 1024 -z 64 -m 262144 -d 1024 -V 0 -n 1024 -r 64 -l 64 -D 0 -o 8 -a 512 -R 0 -c 4096 -i 8 -w 64
--> Connecting to external SuperCollider server: 127.0.0.1:39720
[scynth] Found 0 LADSPA plugins
[scynth] Number of Devices: 6
[scynth]    0 : "BlackHole 2ch"
[scynth]    1 : "MacBook Pro Microphone"
[scynth]    2 : "MacBook Pro Speakers"
[scynth]    3 : "Camo Micro"
[scynth]    4 : "Aggregate Device"
[scynth]    5 : "Multi-Output Device"
[scynth]
[scynth] "BlackHole 2ch" Input Device
[scynth]    Streams: 1
[scynth]       0  channels 2
[scynth]
[scynth] "BlackHole 2ch" Output Device
[scynth]    Streams: 1
[scynth]       0  channels 2
[scynth]
[scynth] SC_AudioDriver: sample rate = 44100.000000, driver's block size = 512
[scynth] SuperCollider 3 server ready.
--> Connection established

    _____                 __
   / __  /_  _____  _____/ /_____  ____  ___
  / / / / | / / _ \/ ___/ __/ __ \/ __ \/ _ \
 / /_/ /| |/ /  __/ /  / /_/ /_/ / / / /  __/
 \____/ |___/\___/_/   \__/\____/_/ /_/\___/

   Collaborative Programmable Music. v0.14.3199


Hello UBERJAR, may this be the start of a beautiful music hacking session...

Overtone config file: /Users/overtone/deploy-example/config/.overtone/config.clj
home dir /Users/overtone/deploy-example/config
--> Initiating shutdown
```
