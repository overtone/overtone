(ns overtone.libs.binary-file-io
  (:import [java.io FileInputStream FileOutputStream File]))

(defn slurp-binary
  "Read in a file containing binary data into a byte-array."
  [f-name]
  (let [f   (File. f-name)
        fis (FileInputStream. f)
        len (.length f)
        ba  (byte-array len)]
    (loop [offset 0]
      (when (< offset len)
        (let [num-read (.read fis ba offset (- len offset))]
          (when (>= num-read 0)
            (recur (+ offset num-read))))))
    (.close fis)
    ba))

(defn spit-binary
  "Write a byte-array into a file with path f-name."
  [f-name bytes]
  (let [f   (File. f-name)
        fos (FileOutputStream. f)]
    (.write fos bytes)
    (.close fos)))
