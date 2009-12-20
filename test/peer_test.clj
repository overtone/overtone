(ns peer-test
  (:use (overtone peer util))
  (:import java.net.InetAddress))

(def PEER-ID (uuid))

(def port (+ 2000 (rand-int 5000)))

(def host (.getHostName (InetAddress/getLocalHost)))
(def ip-addr (.getAddress (InetAddress/getLocalHost)))

(peer-start port)

(println "peer: " PEER-ID)
(println ip-addr port)
