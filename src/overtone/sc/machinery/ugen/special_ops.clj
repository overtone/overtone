(ns
    ^{:doc "Metadata regarding the various functionalities of the unary and binary ugens. These ugens are different to typical ugens in that they receive an 'opcode' as a parameter which defines its behviour - ranging from addition to trig functions to midi->cps conversion."
      :author "Jeff Rose & Sam Aaron"}

  overtone.sc.machinery.ugen.special-ops)

(def UNARY-OPS
  {"neg" 0         ; inversion
   "not-pos?" 1    ; 0 when a < 0, +1 when a > 0, 1 when a is 0
   ;;"is-nil" 2    ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"not-nil" 3   ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"bitNot" 4    ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   "abs" 5         ; absolute value
   ;;"asFloat" 6   ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"asInt"   7   ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   "ceil" 8        ; next higher integer
   "floor" 9       ; next lower integer
   "frac" 10       ; fractional part
   "sign" 11       ; -1 when a < 0, +1 when a > 0, 0 when a is 0
   "squared" 12    ; a*a
   "cubed" 13      ; a*a*a
   "sqrt" 14       ; square root
   "exp" 15        ; exponential
   "reciprocal" 16 ; reciprocal
   "midicps" 17    ; MIDI note number to cycles per second
   "cpsmidi" 18    ; cycles per second to MIDI note number
   "midiratio" 19  ; convert an interval in MIDI notes into a frequency ratio
   "ratiomidi" 20  ; convert a frequency ratio to an interval in MIDI notes
   "dbamp" 21      ; decibels to linear amplitude
   "ampdb" 22      ; linear amplitude to decibels
   "octcps" 23     ; decimal octaves to cycles per second
   "cpsoct" 24     ; cycles per second to decimal octaves
   "log" 25        ; natural logarithm
   "log2" 26       ; base 2 logarithm
   "log10" 27      ; base 10 logarithm
   "sin" 28        ; sine
   "cos" 29        ; cosine
   "tan" 30        ; tangent
   "asin" 31       ; arcsine
   "acos" 32       ; arccosine
   "atan" 33       ; arctangent
   "sinh" 34       ; hyperbolic sine
   "cosh" 35       ; hyperbolic cosine
   "tanh" 36       ; hyperbolic tangent
   ;;"rand" 37     ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"rand2" 38    ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"linrand" 39  ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"bilinrand" 40; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"sum3rand" 41 ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   "distort" 42    ; distortion
   "softclip" 43   ; distortion
   ;;"coin" 44     ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"digit-val" 45; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"silence" 46  ; outputs 0 - no need to include it as we already have the silent ugen
   ;;"thru" 47     ; outputs what was received - not useful
   "rectWindow" 48 ; rectangular window
   "hanWindow" 49  ; hanning window
   "welWindow" 50  ; welch window
   "triWindow" 51  ; triangle window
   ;;"ramp" 52     ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"scurve" 53   ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   })

(def REVERSE-UNARY-OPS (zipmap (vals UNARY-OPS) (keys UNARY-OPS)))

; Commented out ops are implemented with generics instead of generated
; see sc/ops.clj
(def BINARY-OPS
  {
   "+" 0             ; Addition
   "-" 1             ; Subtraction
   "*" 2             ; Multiplication
   ;;"div" 3         ; Defined in BinaryOpUGens.cpp enum but not implemented on the server
   "/" 4             ; Division
   "mod" 5           ; Modulus
   "=" 6             ; Equality
   "not=" 7          ; Inequality
   "<" 8             ; Less than
   ">" 9             ; Greater than
   "<=" 10           ; Less than or equal to
   ">=" 11           ; Greater than or equal to
   "min" 12          ; minimum
   "max" 13          ; maximum
   "and" 14          ; and (where pos sig is true)
   "or" 15           ; or (where pos sig is true
   "xor" 16          ; xor (where pos sig is true)
   ;;"lcm" 17        ; Defined in BinaryOpUGens.cpp enum but not implemented on the server
   ;;"gcd" 18        ; Defined in BinaryOpUGens.cpp enum but not implemented on the server
   "round" 19        ; Round to nearest multiple
   "round-up" 20     ; Round up to next multiple
   "round-down" 21   ; Round down to previous multiple
   "atan2" 22        ; arctangent of a/b
   "hypot" 23        ; length of hypotenuse via Pythag
   "hypot-aprox" 24  ; approximation of length of hypotenuse
   "pow" 25          ; exponentiation
   ;;"leftShift" 26  ; Defined in BinaryOpUGens.cpp enum but not implemented on the server
   ;;"rightShift" 27 ; Defined in BinaryOpUGens.cpp enum but not implemented on the server
   ;;"un-r-shift" 28 ; Defined in BinaryOpUGens.cpp enum but not implemented on the server
   ;;"fill" 29       ; Defined in BinaryOpUGens.cpp enum but not implemented on the server
   "ring1" 30        ; a * (b + 1) == a * b + a
   "ring2" 31        ; a * b + a + b
   "ring3" 32        ; a*a*b
   "ring4" 33        ; a*a*b - a*b*b
   "difsqr" 34       ; a*a - b*b
   "sumsqr" 35       ; a*a + b*b
   "sqrsum" 36       ; (a + b)^2
   "sqrdif" 37       ; (a - b)^2
   "absdif" 38       ; |a - b|
   "thresh" 39       ; Signal thresholding
   "amclip" 40       ; Two quadrant multiply
   "scale-neg" 41    ; scale negative part of input wave
   "clip2" 42        ; bilateral clipping
   "excess" 43       ; clipping residual
   "fold2" 44        ; bilateral folding
   "wrap2" 45        ; bilateral wrapping
   ;;"first-arg" 46  ; Returns the first arg unchanged - not useful.
   ;;"rrand" 47      ; Defined in BinaryOpUGens.cpp enum but not implemented on the server
   ;;"exprand" 48    ; Defined in BinaryOpUGens.cpp enum but not implemented on the server
   })

(def FOLDABLE-BINARY-OPS
  #{"+" "-" "*" "/" "<" ">" "<=" ">=" "min" "max" "and" "or"})

;;the following are Clojure fns that can only take numerical args
(def NUMERICAL-CLOJURE-FNS
  #{"+" "*" "-" "/" "<" ">" "<=" ">=" "min" "max" "mod"})

(def REVERSE-BINARY-OPS (zipmap (vals BINARY-OPS) (keys BINARY-OPS)))

(defn unary-op-num [name]
  (get UNARY-OPS (str name) false))

(defn binary-op-num [name]
  (get BINARY-OPS (str name) false))

(def binary-op-unary-modes
  {"+"    (fn [ugen-fn arg] (ugen-fn 0 arg))
   "-"    (fn [ugen-fn arg] (ugen-fn 0 arg))
   "*"    (fn [ugen-fn arg] (ugen-fn 1 arg))
   "/"    (fn [ugen-fn arg] (ugen-fn 1 arg))
   "="    (fn [ugen-fn arg] (ugen-fn arg arg))
   "not=" (fn [ugen-fn arg] (ugen-fn arg arg))})
