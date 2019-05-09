(ns overtone.helpers.hash)

(defn md5
  "Generate a md5 checksum for the given string"
  [token]
  (let [hash-bytes
        (doto (java.security.MessageDigest/getInstance "MD5")
          (.reset)
          (.update (.getBytes ^java.lang.String token)))]
    (.toString
     (new java.math.BigInteger 1 (.digest hash-bytes))
     16)))
