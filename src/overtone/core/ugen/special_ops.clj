(ns overtone.core.ugen.special-ops)

(def UNARY-OPS
  {"neg" 0          ; inversion
   "abs" 5          ; absolute value
   "asFloat" 6
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
   "tam" 30        ; tangent
   "asin" 31       ; arcsine
   "acos" 32       ; arccosine
   "atan" 33       ; arctangent
   "sinh" 34       ; hyperbolic sine
   "cosh" 35       ; hyperbolic cosine
   "tanh" 36       ; hyperbolic tangent
   "rand2" 38
   "linrand" 39
   "bilinrand" 40
   "sum3rand" 41
   "distort" 42    ; distortion
   "softclip" 43   ; distortion
   "coin" 44
   "rectWindow" 48
   "hanWindow" 49
   "welWindow" 50
   "triWindow" 51
   "ramp" 52
   "scurve" 53})

; The ops that collide with clojure built-ins.
(def UNARY-OPS-COLLIDE
  {"bitNot" 4       ; reciprocal
   "rand" 37})

; Commented out ops are implemented with generics instead of generated
; see core/ops.clj
(def BINARY-OPS
  (apply hash-map [
   "div" 3         ; integer division
   "minimum" 12
   "maximum" 13
   "lcm" 17
   "gcd" 18
   "round" 19
   "roundUp" 20
   "trunc" 21
   "atan2" 22
   "hypot" 23
   "hypotApx" 24
   "pow" 25
   "leftShift" 26
   "rightShift" 27
   "unsignedRightShift" 28
   "ring1" 30
   "ring2" 31
   "ring3" 32
   "ring4" 33
   "difsqr" 34
   "sumsqr" 35
   "sqrsum" 36
   "sqrdif" 37
   "absdif" 38
   "thresh" 39
   "amclip" 40
   "scaleneg" 41
   "clip2" 42
   "excess" 43
   "fold2" 44
   "wrap2" 45
   "rrand" 47
   "exprand" 48]))

; Binary ops that collide with clojure built-ins."
(def BINARY-OPS-COLLIDE
  {
   "+" 0           ; addition
   "-" 1           ; subtraction
   "*" 2           ; multiplication
   "/" 4           ; floating point division
   "mod" 5         ; modulus
   "<=" 10         ; less than or equal
   ">=" 11
   })

(defn unary-op-num [name]
  (get UNARY-OPS (str name) false))

(defn binary-op-num [name]
  (get BINARY-OPS (str name) false))

