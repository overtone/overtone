(ns overtone.core.ugen.basicops)

;; // These Unit Generators are instantiated by math operations on UGens

;; BasicOpUGen : UGen {
;; 	var <operator;

;; //	writeName { arg file;
;; //		var name, opname;
;; //		name = this.class.name.asString;
;; //		opname = operator.asString;
;; //		file.putInt8(name.size + opname.size + 1);
;; //		file.putString(name);
;; //		file.putInt8(0);
;; //		file.putString(opname);
;; //	}
;; 	operator_ { arg op;
;; 		operator = op;
;; 		specialIndex = operator.specialIndex;
;; 	}

;; 	argNamesInputsOffset { ^2 }
;; 	argNameForInputAt { arg i;
;; 		var method = this.class.class.findMethod('new');
;; 		if(method.isNil or: {method.argNames.isNil},{ ^nil });
;; 		^method.argNames.at(i + this.argNamesInputsOffset)
;; 	}
;; 	dumpArgs {
;; 		" ARGS:".postln;
;; 		("   operator:" + operator).postln;
;; 		inputs.do({ arg in,ini;
;; 			("   " ++ (this.argNameForInputAt(ini) ? ini.asString)++":" + in + in.class).postln
;; 		});
;; 	}

;; 	dumpName {
;; 		^synthIndex.asString ++ "_" ++ this.operator
;; 	}
;; }

(def specs
     [
      ;; UnaryOpUGen : BasicOpUGen {	
      ;; 	*new { arg selector, a;
      ;; 		^this.multiNew('audio', selector, a)
      ;; 	}
      ;; 	init { arg theOperator, theInput;
      ;; 		this.operator = theOperator;
      ;; 		rate = theInput.rate;
      ;; 		inputs = theInput.asArray;
      ;; 	}
      ;; }

      {:name "UnaryOpUGen",
       :args [{:name "a"}],
       :rates #{:dr :ir :ar :kr}}

      ;; BinaryOpUGen : BasicOpUGen {		
      ;; 	*new { arg selector, a, b;
      ;; 		^this.multiNew('audio', selector, a, b)
      ;; 	}
      
      ;; 	determineRate { arg a, b;
      ;; 		if (a.rate == \demand, { ^\demand });
      ;; 		if (b.rate == \demand, { ^\demand });
      ;; 		if (a.rate == \audio, { ^\audio });
      ;; 		if (b.rate == \audio, { ^\audio });
      ;; 		if (a.rate == \control, { ^\control });
      ;; 		if (b.rate == \control, { ^\control });
      ;; 		^\scalar
      ;; 	}
      ;; 	*new1 { arg rate, selector, a, b;
      
      ;; 		// eliminate degenerate cases
      ;; 		if (selector == '*', {
      ;; 			if (a == 0.0, { ^0.0 });
      ;; 			if (b == 0.0, { ^0.0 });
      ;; 			if (a == 1.0, { ^b });
      ;; 			if (a == -1.0, { ^b.neg });
      ;; 			if (b == 1.0, { ^a });
      ;; 			if (b == -1.0, { ^a.neg });
      ;; 		},{
      ;; 		if (selector == '+', {
      ;; 			if (a == 0.0, { ^b });
      ;; 			if (b == 0.0, { ^a });
      ;; 		},{
      ;; 		if (selector == '-', {
      ;; 			if (a == 0.0, { ^b.neg });
      ;; 			if (b == 0.0, { ^a });
      ;; 		},{
      ;; 		if (selector == '/', {
      ;; 			if (b == 1.0, { ^a });
      ;; 			if (b == -1.0, { ^a.neg });
      ;; 			if (b.rate == 'scalar', { ^a * b.reciprocal });
      ;; 		})})})});
      
      ;;  		^super.new1(rate, selector, a, b)
      ;; 	}
      
      ;; 	init { arg theOperator, a, b;
      ;; 		this.operator = theOperator;
      ;; 		rate = this.determineRate(a, b);
      ;; 		inputs = [a, b];
      ;; 	}
      
      ;; 	optimizeGraph {
      ;; 		var a, b, muladd;
      ;; 		#a, b = inputs;
      
      ;; 		//this.constantFolding;
      
      ;; 		if (operator == '+', {
      ;; 			// create a MulAdd if possible.
      ;; 			if (a.isKindOf(BinaryOpUGen) and: { a.operator == '*' 
      ;; 				and: { a.descendants.size == 1 }}, 
      ;; 			{
      ;; 				if (MulAdd.canBeMulAdd(a.inputs[0], a.inputs[1], b), {
      ;; 					buildSynthDef.removeUGen(a);
      ;; 					muladd = MulAdd.new(a.inputs[0], a.inputs[1], b);
      ;; 				},{
      ;; 				if (MulAdd.canBeMulAdd(a.inputs[1], a.inputs[0], b), {
      ;; 					buildSynthDef.removeUGen(a);
      ;; 					muladd = MulAdd.new(a.inputs[1], a.inputs[0], b)
      ;; 				})});
      ;; 			},{
      ;; 			if (b.isKindOf(BinaryOpUGen) and: { b.operator == '*' 
      ;; 				and: { b.descendants.size == 1 }}, 
      ;; 			{
      ;; 				if (MulAdd.canBeMulAdd(b.inputs[0], b.inputs[1], a), {
      ;; 					buildSynthDef.removeUGen(b);
      ;; 					muladd = MulAdd.new(b.inputs[0], b.inputs[1], a)
      ;; 				},{
      ;; 				if (MulAdd.canBeMulAdd(b.inputs[1], b.inputs[0], a), {
      ;; 					buildSynthDef.removeUGen(b);
      ;; 					muladd = MulAdd.new(b.inputs[1], b.inputs[0], a)
      ;; 				})});
      ;; 			})});
      ;; 			if (muladd.notNil, {
      ;; 				synthDef.replaceUGen(this, muladd);
      ;; 			});
      ;; 		});
      ;; 	}
      
      ;; 	constantFolding {
      ;; 		var a, b, aa, bb, cc, dd, temp, ac_ops, value;
      
      ;; 		// associative & commutative operators
      ;; 		ac_ops = #['+','*','min','max','&&','||'];
      
      ;; 		if (ac_ops.includes(operator).not) { ^this };
      
      ;; 		#a, b = inputs;
      ;; 		if (a.isKindOf(BinaryOpUGen) and: { operator == a.operator 
      ;; 			and: { b.isKindOf(BinaryOpUGen) and: { operator == b.operator } }}) {
      ;; 			#aa, bb = a.inputs;
      ;; 			#cc, dd = b.inputs;
      ;; 			if (aa.isKindOf(SimpleNumber)) {
      ;; 				if (cc.isKindOf(SimpleNumber)) {
      ;; 					b.inputs[0] = bb;
      ;; 					this.inputs[0] = aa.perform(operator, cc);
      ;; 					synthDef.removeUGen(a);
      ;; 				}{
      ;; 				if (dd.isKindOf(SimpleNumber)) {
      ;; 					b.inputs[1] = bb;
      ;; 					this.inputs[0] = aa.perform(operator, dd);
      ;; 					synthDef.removeUGen(a);
      ;; 				}}
      ;; 			}{
      ;; 			if (bb.isKindOf(SimpleNumber)) {
      ;; 				if (cc.isKindOf(SimpleNumber)) {
      ;; 					b.inputs[0] = aa;
      ;; 					this.inputs[0] = bb.perform(operator, cc);
      ;; 					synthDef.removeUGen(a);
      ;; 				}{
      ;; 				if (dd.isKindOf(SimpleNumber)) {
      ;; 					b.inputs[1] = aa;
      ;; 					this.inputs[0] = bb.perform(operator, dd);
      ;; 					synthDef.removeUGen(a);
      ;; 				}}
      ;; 			}};
      
      ;; 		};
      ;; 		#a, b = inputs;
      ;; 		if (a.isKindOf(BinaryOpUGen) and: { operator == a.operator }) {
      ;; 			#aa, bb = a.inputs;
      ;; 			if (b.isKindOf(SimpleNumber)) {
      ;; 				if (aa.isKindOf(SimpleNumber)) {
      ;; 					buildSynthDef.removeUGen(a);
      ;; 					this.inputs[0] = aa.perform(operator, b);
      ;; 					this.inputs[1] = bb;
      ;; 					^this
      ;; 				};
      ;; 				if (bb.isKindOf(SimpleNumber)) {
      ;; 					buildSynthDef.removeUGen(a);
      ;; 					this.inputs[0] = bb.perform(operator, b);
      ;; 					this.inputs[1] = aa;
      ;; 					^this
      ;; 				};
      ;; 			};
      ;; 			// percolate constants upward so that a subsequent folding may occur
      ;; 			if (aa.isKindOf(SimpleNumber)) {
      ;; 				this.inputs[1] = aa;
      ;; 				a.inputs[0] = b;
      ;; 			}{
      ;; 			if (bb.isKindOf(SimpleNumber)) {
      ;; 				this.inputs[1] = bb;
      ;; 				a.inputs[1] = b;
      ;; 			}};
      ;; 		};
      ;; 		#a, b = inputs;
      ;; 		if (b.isKindOf(BinaryOpUGen) and: { operator == b.operator }) {
      ;; 			#cc, dd = b.inputs;
      ;; 			if (a.isKindOf(SimpleNumber)) {
      ;; 				if (cc.isKindOf(SimpleNumber)) {
      ;; 					buildSynthDef.removeUGen(b);
      ;; 					this.inputs[0] = a.perform(operator, cc);
      ;; 					this.inputs[1] = dd;
      ;; 					^this
      ;; 				};
      ;; 				if (dd.isKindOf(SimpleNumber)) {
      ;; 					buildSynthDef.removeUGen(b);
      ;; 					this.inputs[0] = a.perform(operator, dd);
      ;; 					this.inputs[1] = cc;
      ;; 					^this
      ;; 				};
      ;; 			};
      ;; 			// percolate constants upward so that a subsequent folding may occur
      ;; 			if (cc.isKindOf(SimpleNumber)) {
      ;; 				this.inputs[0] = cc;
      ;; 				b.inputs[0] = a;
      ;; 			}{
      ;; 			if (dd.isKindOf(SimpleNumber)) {
      ;; 				this.inputs[0] = dd;
      ;; 				b.inputs[1] = a;
      ;; 			}};
      ;; 		};
      ;; 		#a, b = inputs;
      ;; 		if (a.isKindOf(SimpleNumber) and: { b.isKindOf(SimpleNumber) }) {
      ;; 			synthDef.replaceUGen(this, a.perform(operator, b));
      ;; 			synthDef.removeUGen(this);
      ;; 		};
      ;; 	}
      ;; }

      {:name "BinaryOpUGen",
       :args [{:name "a"}
              {:name "b"}],
       :rates #{:dr :ir :ar :kr}}

      ;; MulAdd : UGen {
      ;; 	*new { arg in, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', in, mul, add)
      ;; 	}
      ;; 	*new1 { arg rate, in, mul, add;
      ;; 		var minus, nomul, noadd;

      ;; 		// eliminate degenerate cases
      ;;  		if (mul == 0.0, { ^add });
      ;; 		minus = mul == -1.0;
      ;; 		nomul = mul == 1.0;
      ;; 		noadd = add == 0.0;
      ;;  		if (nomul && noadd, { ^in });
      ;;  		if (minus && noadd, { ^in.neg });
      ;;  		if (noadd, { ^in * mul });
      ;;   		if (minus, { ^add - in });
      ;; 		if (nomul, { ^in + add });
      
      ;;  		^super.new1(rate, in, mul, add)
      ;; 	}
      ;; 	init { arg in, mul, add;
      ;; 		rate = in.rate;
      ;; 		inputs = [in, mul, add];
      ;; 	}
      
      ;; 	*canBeMulAdd { arg in, mul, add;
      ;; 		// see if these inputs satisfy the constraints of a MulAdd ugen.
      ;; 		if (in.rate == \audio, { ^true });
      ;; 		if (in.rate == \control 
      ;; 			and: { mul.rate == \control || { mul.rate == \scalar }} 
      ;; 			and: { add.rate == \control || { add.rate == \scalar }}, 
      ;; 		{ 
      ;; 			^true 
      ;; 		});
      ;; 		^false
      ;; 	}
      ;; }

      {:name "MulAdd",
       :args [{:name "in"}
              {:name "mul", :default 1.0}
              {:name "add", :default 0.0}]}])

;;    {:name "!=", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "&", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "*", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "+", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "-", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "/", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "<", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "<=", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "==", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name ">", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;; {:name ">=", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;   {:name "^", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "abs", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "absdif", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "acos", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "amclip", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "ampdb", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "asFloat", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "asInteger", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "asin", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "atan", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "atan2", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "bilinrand", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "bitNot", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "ceil", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "clip2", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "coin", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "cos", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "cosh", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "cpsmidi", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "cpsoct", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "cubed", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "dbamp", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "difsqr", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "digitValue", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "distort", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "div", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "excess", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "exp", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "exprand", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "fill", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "firstArg", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "floor", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "fold2", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "frac", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "gcd", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "hanWindow", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "hypot", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "hypotApx", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "isNil", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "lcm", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "leftShift", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "linrand", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "log", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "log10", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "log2", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "max", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "midicps", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "midiratio", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "min", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "mod", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "neg", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "not", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "notNil", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "octcps", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "pow", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "ramp", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "rand", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "rand2", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "ratiomidi", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "reciprocal", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "rectWindow", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "rightShift", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "ring1", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "ring2", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "ring3", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "ring4", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "round", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "roundUp", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "rrand", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "scaleneg", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "scurve", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "sign", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "silence", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "sin", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "sinh", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "softclip", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "sqrdif", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "sqrsum", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "sqrt", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "squared", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "sum3rand", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "sumsqr", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "tan", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "tanh", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "thresh", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "thru", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "triWindow", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "trunc", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "unsignedRightShift", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "welWindow", :args [{:name "a"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "wrap2", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}

;;    {:name "|", :args [{:name "a"} {:name "b"}], :rates #{:dr :ir :ar :kr}}