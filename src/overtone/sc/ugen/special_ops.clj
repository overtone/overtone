(ns
    ^{:doc "Metadata regarding the various functionalities of the unary and binary ugens. These ugens are different to typical ugens in that they receive an 'opcode' as a parameter which defines its behviour - ranging from addition to trig functions to midi->cps conversion."
      :author "Jeff Rose & Sam Aaron"}

  overtone.sc.ugen.special-ops)

(def UNARY-OPS
  {"neg" 0         ; inversion
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
   "rectWindow" 48
   "hanWindow" 49
   "welWindow" 50
   "triWindow" 51
   ;;"ramp" 52     ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   ;;"scurve" 53   ; Defined in UnaryOpUGens.cpp enum but not implemented on the server
   })

(def REVERSE-UNARY-OPS (zipmap (vals UNARY-OPS) (keys UNARY-OPS)))

; Commented out ops are implemented with generics instead of generated
; see sc/ops.clj
(def BINARY-OPS
  {
   "+" 0
   "-" 1
   "*" 2
   "div" 3         ; integer division
   "/" 4
   "mod" 5
   "=" 6
   "!=" 7
   "<" 8
   ">" 9
   "<=" 10
   ">=" 11
   "min" 12
   "max" 13
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
   "exprand" 48})

(def REVERSE-BINARY-OPS (zipmap (vals BINARY-OPS) (keys BINARY-OPS)))

(defn unary-op-num [name]
  (get UNARY-OPS (str name) false))

(defn binary-op-num [name]
  (get BINARY-OPS (str name) false))
