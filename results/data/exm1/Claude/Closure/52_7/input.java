// buggy code
  static boolean isSimpleNumber(String s) {
    int len = s.length();
    for (int index = 0; index < len; index++) {
      char c = s.charAt(index);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return len > 0;
  }

// relevant test
// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous1
  public void testAnonymous1() {
    testSame("function foo() {}; function bar() {}; foo(function() {bar()})");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous2
  public void testAnonymous2() {
    test("var foo;(function(){foo=6;})()", "(function(){})()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous3
  public void testAnonymous3() {
    testSame("var foo; (function(){ if(!foo)foo=6; })()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous4
  public void testAnonymous4() {
    testSame("var foo; (function(){ foo=6; })(); externfoo=foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous5
  public void testAnonymous5() {
    testSame("var foo;" +
             "(function(){ foo=function(){ bar() }; function bar(){} })();" +
             "foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous6
  public void testAnonymous6() {
    testSame("function foo(){}" +
             "function bar(){}" +
             "foo(function(){externfoo = bar});");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous7
  public void testAnonymous7() {
    testSame("var foo;" +
             "(function (){ function bar(){ externfoo = foo; } bar(); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous8
  public void testAnonymous8() {
    testSame("var foo;" +
             "(function (){ var g=function(){ externfoo = foo; }; g(); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous9
  public void testAnonymous9() {
    testSame("function foo(){}" +
             "function bar(){}" +
             "foo(function(){ function baz(){ externfoo = bar; } baz(); });");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctions1
  public void testFunctions1() {
    testSame("var foo = null; function baz() {}" +
             "function bar() {foo=baz();} bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctions2
  public void testFunctions2() {
    testSame("var foo; foo = function() {var a = bar()};" +
             "var bar = function(){}; foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem1
  public void testGetElem1() {
    testSame("var foo = {}; foo.bar = {}; foo.bar.baz = {a: 5, b: 10};" +
             "var fn = function() {window[foo.bar.baz.a] = 5;}; fn()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem2
  public void testGetElem2() {
    testSame("var foo = {}; foo.bar = {}; foo.bar.baz = {a: 5, b: 10};" +
             "var fn = function() {this[foo.bar.baz.a] = 5;}; fn()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem3
  public void testGetElem3() {
    testSame("var foo = {'i': 0, 'j': 1}; foo['k'] = 2; top.foo = foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf1
  public void testIf1() {
    test("var foo = {};if(e)foo.bar=function(){};", "if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf2
  public void testIf2() {
    test("var e = false;var foo = {};if(e)foo.bar=function(){};",
         "var e = false;if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf3
  public void testIf3() {
    test("var e = false;var foo = {};if(e + 1)foo.bar=function(){};",
         "var e = false;if(e + 1);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf4
  public void testIf4() {
    test("var e = false, f;var foo = {};if(f=e)foo.bar=function(){};",
         "var e = false;if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf5
  public void testIf5() {
    test("var e = false, f;var foo = {};if(f = e + 1)foo.bar=function(){};",
         "var e = false;if(e + 1);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIfElse
  public void testIfElse() {
    test("var foo = {};if(e)foo.bar=function(){};else foo.bar=function(){};",
         "if(e);else;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testWhile
  public void testWhile() {
    test("var foo = {};while(e)foo.bar=function(){};", "while(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFor
  public void testFor() {
    test("var foo = {};for(e in x)foo.bar=function(){};", "for(e in x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDo
  public void testDo() {
    test("var cond = false;do {var a = 1} while (cond)",
         "var cond = false;do {} while (cond)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct1
  public void testSetterInForStruct1() {
    test("var j = 0; for (var i = 1; i = 0; j++);",
         "var j = 0; for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct2
  public void testSetterInForStruct2() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.prototype.property_ = 0; i++);",
         "for (var i = 1; 0; i++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct3
  public void testSetterInForStruct3() {
    test("var j = 0; for (var i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; f(); g(); h(); for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct4
  public void testSetterInForStruct4() {
    test("var i = 0;var j = 0; for (i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; f(); g(); h(); for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct5
  public void testSetterInForStruct5() {
    test("var i = 0, j = 0; for (i = f(), j = g(); 0;);",
         "for (f(), g(); 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct6
  public void testSetterInForStruct6() {
    test("var i = 0, j = 0, k = 0; for (i = f(), j = g(), k = h(); i = 0;);",
         "for (f(), g(), h(); 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct7
  public void testSetterInForStruct7() {
    test("var i = 0, j = 0, k = 0; for (i = 1, j = 2, k = 3; i = 0;);",
         "for (1, 2, 3; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct8
  public void testSetterInForStruct8() {
    test("var i = 0, j = 0, k = 0; for (i = 1, j = i, k = 2; i = 0;);",
         "var i = 0; for(i = 1, i , 2; i = 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct9
  public void testSetterInForStruct9() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.property_ = 0; i++);",
         "for (var i = 1; 0; i++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct10
  public void testSetterInForStruct10() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.property_ = 0; i = 2);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct11
  public void testSetterInForStruct11() {
    test("var Class = function() {}; " +
         "for (;Class.property_ = 0;);",
         "for (;0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct12
  public void testSetterInForStruct12() {
    test("var a = 1; var Class = function() {}; " +
         "for (;Class.property_ = a;);",
         "var a = 1; for (; a;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct13
  public void testSetterInForStruct13() {
    test("var a = 1; var Class = function() {}; " +
         "for (Class.property_ = a; 0 ;);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct14
  public void testSetterInForStruct14() {
    test("var a = 1; var Class = function() {}; " +
         "for (; 0; Class.property_ = a);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct15
  public void testSetterInForStruct15() {
    test("var Class = function() {}; " +
         "for (var i = 1; 0; Class.prototype.property_ = 0);",
         "for (; 0; 0);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct16
  public void testSetterInForStruct16() {
    test("var Class = function() {}; " +
         "for (var i = 1; i = 0; Class.prototype.property_ = 0);",
         "for (; 0; 0);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn1
  public void testSetterInForIn1() {
    test("var foo = {}; var bar; for(e in bar = foo.a);",
         "var foo = {}; for(e in foo.a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn2
  public void testSetterInForIn2() {
    testSame("var foo = {}; var bar; for(e in bar = foo.a); bar");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn3
  public void testSetterInForIn3() {
    
    
    test("var foo = {}; var bar; for(e in bar = foo.a); bar.b = 3",
         "var foo = {}; for(e in foo.a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn4
  public void testSetterInForIn4() {
    
    
    test("var foo = {}; var bar; for (e in bar = foo.a); bar.b = 3; foo.a",
         "var foo = {}; for (e in foo.a); foo.a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn5
  public void testSetterInForIn5() {
    
    
    test("var foo = {}; var bar; for (e in foo.a) { bar = e } bar.b = 3; foo.a",
         "var foo={};for(e in foo.a);foo.a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn6
  public void testSetterInForIn6() {
    testSame("var foo = {};for(e in foo);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInIfPredicate
  public void testSetterInIfPredicate() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "if (Class.property_ = a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInWhilePredicate
  public void testSetterInWhilePredicate() {
    test("var a = 1;" +
         "var Class = function() {}; " +
         "while (Class.property_ = a);",
         "var a = 1; for (;a;) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInDoWhilePredicate
  public void testSetterInDoWhilePredicate() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "do {} while(Class.property_ = a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInSwitchInput
  public void testSetterInSwitchInput() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "switch (Class.property_ = a) {" +
             "  default:" +
             "}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexAssigns
  public void testComplexAssigns() {
    
    testSame("var x = 0; x += 3; x *= 5;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssigns
  public void testNestedAssigns() {
    test("var x = 0; var y = x = 3; window.alert(y);",
         "var y = 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns1
  public void testComplexNestedAssigns1() {
    
    testSame("var x = 0; var y = 2; y += x = 3; window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns2
  public void testComplexNestedAssigns2() {
    test("var x = 0; var y = 2; y += x = 3; window.alert(y);",
         "var y = 2; y += 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns3
  public void testComplexNestedAssigns3() {
    test("var x = 0; var y = x += 3; window.alert(x);",
         "var x = 0; x += 3; window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns4
  public void testComplexNestedAssigns4() {
    testSame("var x = 0; var y = x += 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope1
  public void testUnintendedUseOfInheritsInLocalScope1() {
    testSame("goog.mixin = function() {}; " +
             "(function() { var x = {}; var y = {}; goog.mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope2
  public void testUnintendedUseOfInheritsInLocalScope2() {
    testSame("goog.mixin = function() {}; " +
             "var x = {}; var y = {}; (function() { goog.mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope3
  public void testUnintendedUseOfInheritsInLocalScope3() {
    testSame("goog.mixin = function() {}; " +
             "var x = {}; var y = {}; (function() { goog.mixin(x, y); })(); " +
             "window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope4
  public void testUnintendedUseOfInheritsInLocalScope4() {
    
    
    testSame("var goog$mixin = function() {}; " +
             "(function() { var x = {}; var y = {}; goog$mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope1
  public void testPrototypePropertySetInLocalScope1() {
    testSame("(function() { var x = function(){}; x.prototype.bar = 3; })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope2
  public void testPrototypePropertySetInLocalScope2() {
    testSame("var x = function(){}; (function() { x.prototype.bar = 3; })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope3
  public void testPrototypePropertySetInLocalScope3() {
    test("var x = function(){ x.prototype.bar = 3; };", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope4
  public void testPrototypePropertySetInLocalScope4() {
    test("var x = {}; x.foo = function(){ x.foo.prototype.bar = 3; };", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope5
  public void testPrototypePropertySetInLocalScope5() {
    test("var x = {}; x.prototype.foo = 3;", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope6
  public void testPrototypePropertySetInLocalScope6() {
    testSame("var x = {}; x.prototype.foo = 3; bar(x.prototype.foo)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope7
  public void testPrototypePropertySetInLocalScope7() {
    testSame("var x = {}; x.foo = 3; bar(x.foo)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference1
  public void testRValueReference1() {
    testSame("var a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference2
  public void testRValueReference2() {
    testSame("var a = 1; 1+a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference3
  public void testRValueReference3() {
    testSame("var x = {}; x.prototype.foo = 3; var a = x.prototype.foo; 1+a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference4
  public void testRValueReference4() {
    testSame("var x = {}; x.prototype.foo = 3; x.prototype.foo");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference5
  public void testRValueReference5() {
    testSame("var x = {}; x.prototype.foo = 3; 1+x.prototype.foo");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference6
  public void testRValueReference6() {
    testSame("var x = {}; var idx = 2; x[idx]");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnhandledTopNode
  public void testUnhandledTopNode() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
             "function Bar() {}; Bar.prototype.isFoo = function() {};" +
             "var foo = new Foo(); var bar = new Bar();" +
             
             
             "var cond = foo.isBar() && bar.isFoo();" +
             "if (cond) {window.alert('hello');}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPropertyDefinedInGlobalScope
  public void testPropertyDefinedInGlobalScope() {
    testSame("function Foo() {}; var x = new Foo(); x.cssClass = 'bar';" +
             "window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConditionallyDefinedFunction1
  public void testConditionallyDefinedFunction1() {
    testSame("var g; externfoo.x || (externfoo.x = function() { g; })");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConditionallyDefinedFunction2
  public void testConditionallyDefinedFunction2() {
    testSame("var g; 1 || (externfoo.x = function() { g; })");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElemOnThis
  public void testGetElemOnThis() {
    testSame("var a = 3; this['foo'] = a;");
    testSame("this['foo'] = 3;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveInstanceOfOnly
  public void testRemoveInstanceOfOnly() {
    test("function Foo() {}; Foo.prototype.isBar = function() {};" +
         "var x; if (x instanceof Foo) { window.alert(x); }",
         ";var x; if (false) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLocalScopedInstanceOfOnly
  public void testRemoveLocalScopedInstanceOfOnly() {
    test("function Foo() {}; function Bar(x) { this.z = x instanceof Foo; };" +
        "externfoo.x = new Bar({});",
        ";function Bar(x) { this.z = false }; externfoo.x = new Bar({});");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveInstanceOfWithReferencedMethod
  public void testRemoveInstanceOfWithReferencedMethod() {
    test("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "var x; if (x instanceof Foo) { window.alert(x.isBar()); }",
        ";var x; if (false) { window.alert(x.isBar()); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeReferencedInstanceOf
  public void testDoNotChangeReferencedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
             "var x = new Foo(); if (x instanceof Foo) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeReferencedLocalScopedInstanceOf
  public void testDoNotChangeReferencedLocalScopedInstanceOf() {
    testSame("function Foo() {}; externfoo.x = new Foo();" +
        "function Bar() { if (x instanceof Foo) { window.alert(x); } };" +
        "externfoo.y = new Bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeLocalScopeReferencedInstanceOf
  public void testDoNotChangeLocalScopeReferencedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "function Bar() { this.z = new Foo(); }; externfoo.x = new Bar();" +
        "if (x instanceof Foo) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeLocalScopeReferencedLocalScopedInstanceOf
  public void testDoNotChangeLocalScopeReferencedLocalScopedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "function Bar() { this.z = new Foo(); };" +
        "Bar.prototype.func = function(x) {" +
          "if (x instanceof Foo) { window.alert(x); }" +
        "}; new Bar().func();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeInstanceOfGetElem
  public void testDoNotChangeInstanceOfGetElem() {
    testSame("var goog = {};" +
        "function f(obj, name) {" +
        "  if (obj instanceof goog[name]) {" +
        "    return name;" +
        "  }" +
        "}" +
        "window['f'] = f;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testWeirdnessOnLeftSideOfPrototype
  public void testWeirdnessOnLeftSideOfPrototype() {
    
    
    testSame("var x = 3; " +
        "(function() { this.bar = 3; }).z = function() {" +
        "  return x;" +
        "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit1
  public void testShortCircuit1() {
    test("var a = b() || 1", "b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit2
  public void testShortCircuit2() {
    test("var a = 1 || c()", "1 || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit3
  public void testShortCircuit3() {
    test("var a = b() || c()", "b() || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit4
  public void testShortCircuit4() {
    test("var a = b() || 3 || c()", "b() || 3 || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit5
  public void testShortCircuit5() {
    test("var a = b() && 1", "b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit6
  public void testShortCircuit6() {
    test("var a = 1 && c()", "1 && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit7
  public void testShortCircuit7() {
    test("var a = b() && c()", "b() && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit8
  public void testShortCircuit8() {
    test("var a = b() && 3 && c()", "b() && 3 && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference1
  public void testRhsReference1() {
    testSame("var a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference2
  public void testRhsReference2() {
    testSame("var a = 1; a || b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference3
  public void testRhsReference3() {
    testSame("var a = 1; 1 || a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference4
  public void testRhsReference4() {
    test("var a = 1; var b = a || foo()", "var a = 1; a || foo()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference5
  public void testRhsReference5() {
    test("var a = 1, b = 5; a; foo(b)", "var a = 1, b = 5; a; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign1
  public void testRhsAssign1() {
    test("var foo, bar; foo || (bar = 1)",
         "var foo; foo || 1");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign2
  public void testRhsAssign2() {
    test("var foo, bar, baz; foo || (baz = bar = 1)",
         "var foo; foo || 1");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign3
  public void testRhsAssign3() {
    testSame("var foo = null; foo || (foo = 1)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign4
  public void testRhsAssign4() {
    test("var foo = null; foo = (foo || 1)", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign5
  public void testRhsAssign5() {
    test("var a = 3, foo, bar; foo || (bar = a)", "var a = 3, foo; foo || a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign6
  public void testRhsAssign6() {
    test("function Foo(){} var foo = null;" +
         "var f = function () {foo || (foo = new Foo()); return foo}",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign7
  public void testRhsAssign7() {
    testSame("function Foo(){} var foo = null;" +
             "var f = function () {foo || (foo = new Foo())}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign8
  public void testRhsAssign8() {
    testSame("function Foo(){} var foo = null;" +
             "var f = function () {(foo = new Foo()) || g()}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign9
  public void testRhsAssign9() {
    test("function Foo(){} var foo = null;" +
         "var f = function () {1 + (foo = new Foo()); return foo}",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithOr1
  public void testAssignWithOr1() {
    testSame("var foo = null;" +
        "var f = window.a || function () {return foo}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithOr2
  public void testAssignWithOr2() {
    test("var foo = null;" +
        "var f = window.a || function () {return foo};",
        "var foo = null"); 
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithAnd1
  public void testAssignWithAnd1() {
    testSame("var foo = null;" +
        "var f = window.a && function () {return foo}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithAnd2
  public void testAssignWithAnd2() {
    test("var foo = null;" +
        "var f = window.a && function () {return foo};",
        "var foo = null;");  
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook1
  public void testAssignWithHook1() {
    testSame("function Foo(){} var foo = null;" +
        "var f = window.a ? " +
        "    function () {return new Foo()} : function () {return foo}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook2
  public void testAssignWithHook2() {
    test("function Foo(){} var foo = null;" +
        "var f = window.a ? " +
        "    function () {return new Foo()} : function () {return foo};",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook3
  public void testAssignWithHook3() {
    testSame("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? " +
        "    function () {return new Foo()} : function () {return foo}; f.b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook4
  public void testAssignWithHook4() {
    test("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? " +
        "    function () {return new Foo()} : function () {return foo};",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook5
  public void testAssignWithHook5() {
    testSame("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? function () {return new Foo()} :" +
        "    window.b ? function () {return foo} :" +
        "    function() { return Foo }; f.b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook6
  public void testAssignWithHook6() {
    test("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? function () {return new Foo()} :" +
        "    window.b ? function () {return foo} :" +
        "    function() { return Foo };",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign1
  public void testNestedAssign1() {
    test("var a, b = a = 1, c = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign2
  public void testNestedAssign2() {
    test("var a, b = a = 1; foo(b)",
         "var b = 1; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign3
  public void testNestedAssign3() {
    test("var a, b = a = 1; a = b = 2; foo(b)",
         "var b = 1; b = 2; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign4
  public void testNestedAssign4() {
    test("var a, b = a = 1; b = a = 2; foo(b)",
         "var b = 1; b = 2; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign5
  public void testNestedAssign5() {
    test("var a, b = a = 1; b = a = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign15
  public void testNestedAssign15() {
    test("var a, b, c; c = b = a = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign6
  public void testNestedAssign6() {
    testSame("var a, b, c; a = b = c = 1; foo(a, b, c)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign7
  public void testNestedAssign7() {
    testSame("var a = 0; a = i[j] = 1; b(a, i[j])");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign8
  public void testNestedAssign8() {
    testSame("function f(){" +
             "this.lockedToken_ = this.lastToken_ = " +
             "SETPROP_value(this.hiddenInput_, a)}f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain1
  public void testRefChain1() {
    test("var a = 1; var b = a; var c = b; var d = c", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain2
  public void testRefChain2() {
    test("var a = 1; var b = a; var c = b; var d = c || f()",
         "var a = 1; var b = a; var c = b; c || f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain3
  public void testRefChain3() {
    test("var a = 1; var b = a; var c = b; var d = c + f()", "f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain4
  public void testRefChain4() {
    test("var a = 1; var b = a; var c = b; var d = f() || c",
         "f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain5
  public void testRefChain5() {
    test("var a = 1; var b = a; var c = b; var d = f() ? g() : c",
         "f() && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain6
  public void testRefChain6() {
    test("var a = 1; var b = a; var c = b; var d = c ? f() : g()",
         "var a = 1; var b = a; var c = b; c ? f() : g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain7
  public void testRefChain7() {
    test("var a = 1; var b = a; var c = b; var d = (b + f()) ? g() : c",
         "var a = 1; var b = a; (b+f()) && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain8
  public void testRefChain8() {
    test("var a = 1; var b = a; var c = b; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain9
  public void testRefChain9() {
    test("var a = 1; var b = a; var c = 5; var d = f()[b+c] ? g() : 0",
         "var a = 1; var b = a; var c = 5; f()[b+c] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain10
  public void testRefChain10() {
    test("var a = 1; var b = a; var c = b; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain11
  public void testRefChain11() {
    test("var a = 1; var b = a; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain12
  public void testRefChain12() {
    testSame("var a = 1; var b = a; f()[b] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain13
  public void testRefChain13() {
    test("function f(){}var a = 1; var b = a; var d = f()[b] ? g() : 0",
         "function f(){}var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain14
  public void testRefChain14() {
    testSame("function f(){}var a = 1; var b = a; f()[b] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain15
  public void testRefChain15() {
    test("function f(){}var a = 1, b = a; var c = f(); var d = c[b] ? g() : 0",
         "function f(){}var a = 1, b = a; var c = f(); c[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain16
  public void testRefChain16() {
    testSame("function f(){}var a = 1; var b = a; var c = f(); c[b] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain17
  public void testRefChain17() {
    test("function f(){}var a = 1; var b = a; var c = f(); var d = c[b]",
         "function f(){} f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain18
  public void testRefChain18() {
    testSame("var a = 1; f()[a] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain19
  public void testRefChain19() {
    test("var a = 1; var b = [a]; var c = b; b[f()] ? g() : 0",
         "var a=1; var b=[a]; b[f()] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain20
  public void testRefChain20() {
    test("var a = 1; var b = [a]; var c = b; var d = b[f()] ? g() : 0",
         "var a=1; var b=[a]; b[f()]&&g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain21
  public void testRefChain21() {
    testSame("var a = 1; var b = 2; var c = a + b; f(c)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain22
  public void testRefChain22() {
    test("var a = 2; var b = a = 4; f(a)", "var a = 2; a = 4; f(a)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain23
  public void testRefChain23() {
    test("var a = {}; var b = a[1] || f()", "var a = {}; a[1] || f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs
  public void testAssignmentWithComplexLhs() {
    testSame("function f() { return this; }" +
             "var o = {'key': 'val'};" +
             "f().x_ = o['key'];");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs2
  public void testAssignmentWithComplexLhs2() {
    testSame("function f() { return this; }" +
             "var o = {'key': 'val'};" +
             "f().foo = function() {" +
             "  o" +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs3
  public void testAssignmentWithComplexLhs3() {
    String source =
        "var o = {'key': 'val'};" +
        "function init_() {" +
        "  this.x = o['key']" +
        "}";

    test(source, "");
    testSame(source + ";init_()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs4
  public void testAssignmentWithComplexLhs4() {
    testSame("function f() { return this; }" +
             "var o = {'key': 'val'};" +
             "f().foo = function() {" +
             "  this.x = o['key']" +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope1
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope1() {
    testSame("function f(arg){}" +
             "" +
             "(function(){" +
             "  var O = {};" +
             "  O.prototype = 'foo';" +
             "  f(O);" +
             "})()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope2
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope2() {
    testSame("function f(arg){}" +
             "(function h(){" +
             "  var L = {};" +
             "  L.prototype = 'foo';" +
             "  f(L);" +
             "})()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope4
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope4() {
    testSame("function f(arg){}" +
             "function g(){" +
             "  var N = {};" +
             "  N.prototype = 'foo';" +
             "  f(N);" +
             "}" +
             "g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope5
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope5() {
    
    testSame("function g(){ var R = {}; R.prototype = 'foo' } g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemovePrototypeDefinitionsInGlobalScope1
  public void testRemovePrototypeDefinitionsInGlobalScope1() {
    testSame("function f(arg){}" +
             "var M = {};" +
             "M.prototype = 'foo';" +
             "f(M);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemovePrototypeDefinitionsInGlobalScope2
  public void testRemovePrototypeDefinitionsInGlobalScope2() {
    test("var Q = {}; Q.prototype = 'foo'", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment
  public void testRemoveLabeledStatment() {
    test("LBL: var x = 1;", "LBL: {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment2
  public void testRemoveLabeledStatment2() {
    test("var x; LBL: x = f() + g()", "LBL: { f() ; g()}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment3
  public void testRemoveLabeledStatment3() {
    test("var x; LBL: x = 1;", "LBL: {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment4
  public void testRemoveLabeledStatment4() {
    test("var a; LBL: a = f()", "LBL: f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias1
  public void testPreservePropertyMutationsToAlias1() {
    
    
    
    testSame("var a = {}; var b = a; b.x = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias2
  public void testPreservePropertyMutationsToAlias2() {
    
    test("var a = {}; var b = a; var c = a; b.x = 1; a",
         "var a = {}; var b = a; b.x = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias3
  public void testPreservePropertyMutationsToAlias3() {
    
    testSame("var a = {}; var b = a; var c = b; c.x = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias4
 public void testPreservePropertyMutationsToAlias4() {
    
    testSame("var a = {}; var b = a; b['x'] = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias5
  public void testPreservePropertyMutationsToAlias5() {
    
    testSame("function testCall(o){}" +
             "var DATA = {'prop': 'foo','attr': {}};" +
             "var SUBDATA = DATA['attr'];" +
             "SUBDATA['subprop'] = 'bar';" +
             "testCall(DATA);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias6
  public void testPreservePropertyMutationsToAlias6() {
    
    testSame("function testCall(o){}" +
             "var DATA = {'prop': 'foo','attr': {}};" +
             "var SUBDATA = DATA['attr'];" +
             "var SUBSUBDATA = SUBDATA['subprop'];" +
             "SUBSUBDATA['subsubprop'] = 'bar';" +
             "testCall(DATA);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias7
  public void testPreservePropertyMutationsToAlias7() {
    
    test("var a = {}; var b = {}; b.x = 0;" +
         "var goog = {}; goog.inherits(b, a); a",
         "var a = {}; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias8
  public void testPreservePropertyMutationsToAlias8() {
    
    test("var a = {};" +
         "var b = {}; b.x = 0;" +
         "var c = {}; c.y = 0;" +
         "var goog = {}; goog.inherits(b, a); goog.inherits(c, a); c",
         "var a = {}; var c = {}; c.y = 0;" +
         "var goog = {}; goog.inherits(c, a); c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias9
  public void testPreservePropertyMutationsToAlias9() {
    testSame("var a = {b: {}};" +
         "var c = a.b; c.d = 3;" +
         "a.d = 3; a.d;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveAlias
  public void testRemoveAlias() {
    test("var a = {b: {}};" +
         "var c = a.b;" +
         "a.d = 3; a.d;",
         "var a = {b: {}}; a.d = 3; a.d;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSingletonGetter1
  public void testSingletonGetter1() {
    test("function Foo() {} goog.addSingletonGetter(Foo);", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSingletonGetter2
  public void testSingletonGetter2() {
    test("function Foo() {} goog$addSingletonGetter(Foo);", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSingletonGetter3
  public void testSingletonGetter3() {
    
    testSame("function Foo() {} goog$addSingletonGetter(Foo);" +
        "this.x = Foo.getInstance();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias1
  public void testNoRemoveWindowPropertyAlias1() {
     testSame(
         "var self_ = window.gbar;\n" +
         "self_.qs = function() {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias2
  public void testNoRemoveWindowPropertyAlias2() {
    testSame(
        "var self_ = window;\n" +
        "self_.qs = function() {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias3
  public void testNoRemoveWindowPropertyAlias3() {
    testSame(
        "var self_ = window;\n" +
        "self_['qs'] = function() {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias0
  public void testNoRemoveAlias0() {
    testSame(
        "var x = {}; function f() { return x; }; " +
        "f().style.display = 'block';" +
        "alert(x.style)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias1
  public void testNoRemoveAlias1() {
    testSame(
        "var x = {}; function f() { return x; };" +
        "var map = f();\n" +
        "map.style.display = 'block';" +
        "alert(x.style)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias2
  public void testNoRemoveAlias2() {
    testSame(
        "var x = {};" +
        "var map = (function () { return x; })();\n" +
        "map.style = 'block';" +
        "alert(x.style)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias3
  public void testNoRemoveAlias3() {
    testSame(
        "var x = {}; function f() { return x; };" +
        "var map = {}\n" +
        "map[1] = f();\n" +
        "map[1].style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAliasOfExternal0
  public void testNoRemoveAliasOfExternal0() {
    testSame(
        "document.getElementById('foo').style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAliasOfExternal1
  public void testNoRemoveAliasOfExternal1() {
    testSame(
        "var map = document.getElementById('foo');\n" +
        "map.style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAliasOfExternal2
  public void testNoRemoveAliasOfExternal2() {
    testSame(
        "var map = {}\n" +
        "map[1] = document.getElementById('foo');\n" +
        "map[1].style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveThrowReference1
  public void testNoRemoveThrowReference1() {
    testSame(
      "var e = {}\n" +
      "throw e;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveThrowReference2
  public void testNoRemoveThrowReference2() {
    testSame(
      "function e() {}\n" +
      "throw new e();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit1
  public void testClassDefinedInObjectLit1() {
    test(
      "var data = {Foo: function() {}};" +
      "data.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit2
  public void testClassDefinedInObjectLit2() {
    test(
      "var data = {}; data.bar = {Foo: function() {}};" +
      "data.bar.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit3
  public void testClassDefinedInObjectLit3() {
    test(
      "var data = {bar: {Foo: function() {}}};" +
      "data.bar.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit4
  public void testClassDefinedInObjectLit4() {
    test(
      "var data = {};" +
      "data.baz = {bar: {Foo: function() {}}};" +
      "data.baz.bar.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testVarReferencedInClassDefinedInObjectLit1
  public void testVarReferencedInClassDefinedInObjectLit1() {
    testSame(
      "var ref = 3;" +
      "var data = {Foo: function() { this.x = ref; }};" +
      "window.Foo = data.Foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testVarReferencedInClassDefinedInObjectLit2
  public void testVarReferencedInClassDefinedInObjectLit2() {
    testSame(
      "var ref = 3;" +
      "var data = {Foo: function() { this.x = ref; }," +
      "            Bar: function() {}};" +
      "window.Bar = data.Bar;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testArrayExt
  public void testArrayExt() {
    testSame(
      "Array.prototype.foo = function() { return 1 };" +
      "var y = [];" +
      "switch (y.foo()) {" +
      "}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testArrayAliasExt
  public void testArrayAliasExt() {
    testSame(
      "Array$X = Array;" +
      "Array$X.prototype.foo = function() { return 1 };" +
      "function Array$X() {}" +
      "var y = [];" +
      "switch (y.foo()) {" +
      "}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternalAliasInstanceof1
  public void testExternalAliasInstanceof1() {
    test(
      "Array$X = Array;" +
      "function Array$X() {}" +
      "var y = [];" +
      "if (y instanceof Array) {}",
      "var y = [];" +
      "if (y instanceof Array) {}"
      );
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternalAliasInstanceof2
  public void testExternalAliasInstanceof2() {
    testSame(
      "Array$X = Array;" +
      "function Array$X() {}" +
      "var y = [];" +
      "if (y instanceof Array$X) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternalAliasInstanceof3
  public void testExternalAliasInstanceof3() {
    testSame(
      "var b = Array;" +
      "var y = [];" +
      "if (y instanceof b) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAliasInstanceof4
  public void testAliasInstanceof4() {
    testSame(
      "function Foo() {};" +
      "var b = Foo;" +
      "var y = new Foo();" +
      "if (y instanceof b) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAliasInstanceof5
  public void testAliasInstanceof5() {
    
    test(
      "function Foo() {}" +
      "function Bar() {}" +
      "var b = x ? Foo : Bar;" +
      "var y = new Foo();" +
      "if (y instanceof b) {}",
      "function Foo() {}" +
      "var y = new Foo;" +
      "if (false){}");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testSimpleVarAssignment
  public void testSimpleVarAssignment() {
    test("var a = function() { return 1; }",
         "var a = function $() { return 1; }");
    assertMapping("$", "a");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToProperty
  public void testAssignmentToProperty() {
    test("var a = {}; a.b = function() { return 1; }",
         "var a = {}; a.b = function $() { return 1; }");
    assertMapping("$", "a.b");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPrototype
  public void testAssignmentToPrototype() {
    test("function a() {} a.prototype.b = function() { return 1; };",
         "function a() {} " +
         "a.prototype.b = function $() { return 1; };");
    assertMapping("$", "a.prototype.b");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPrototype2
  public void testAssignmentToPrototype2() {
    test("var a = {}; " +
         "a.b = function() {}; " +
         "a.b.prototype.c = function() { return 1; };",
         "var a = {}; " +
         "a.b = function $() {}; " +
         "a.b.prototype.c = function $a() { return 1; };");
    assertMapping("$", "a.b", "$a", "a.b.prototype.c");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPrototype3
  public void testAssignmentToPrototype3() {
    test("function a() {} a.prototype['XXX'] = function() { return 1; };",
         "function a() {} " +
         "a.prototype['XXX'] = function $() { return 1; };");
    assertMapping("$", "a.prototype[\"XXX\"]");
    test("function a() {} a.prototype['\\n'] = function() { return 1; };",
         "function a() {} " +
         "a.prototype['\\n'] = function $() { return 1; };");
    assertMapping("$", "a.prototype[\"\\n\"]");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPrototype4
  public void testAssignmentToPrototype4() {
    test("var Y = 1; function a() {} " +
         "a.prototype[Y] = function() { return 1; };",
         "var Y = 1; function a() {} " +
         "a.prototype[Y] = function $() { return 1; };");
    assertMapping("$", "a.prototype[Y]");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPrototype5
  public void testAssignmentToPrototype5() {
    test("function a() {} a['prototype'].b = function() { return 1; };",
         "function a() {} " +
         "a['prototype'].b = function $() { return 1; };");
    assertMapping("$", "a[\"prototype\"].b");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testPrototypeInitializer
  public void testPrototypeInitializer() {
    test("function a(){} a.prototype = {b: function() { return 1; }};",
         "function a(){} " +
         "a.prototype = {b: function $() { return 1; }};");
    assertMapping("$", "a.prototype.b");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPropertyOfCallReturnValue
  public void testAssignmentToPropertyOfCallReturnValue() {
    test("document.getElementById('x').onClick = function() {};",
         "document.getElementById('x').onClick = " +
         "function $() {};");
    assertMapping("$", "document.getElementById(\"x\").onClick");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPropertyOfArrayElement
  public void testAssignmentToPropertyOfArrayElement() {
    test("var a = {}; a.b = [{}]; a.b[0].c = function() {};",
         "var a = {}; a.b = [{}]; a.b[0].c = function $() {};");
    assertMapping("$", "a.b[0].c");
    test("var a = {b: {'c': {}}}; a.b['c'].d = function() {};",
         "var a = {b: {'c': {}}}; a.b['c'].d = function $() {};");
    assertMapping("$", "a.b[\"c\"].d");
    test("var a = {b: {'c': {}}}; a.b[x()].d = function() {};",
         "var a = {b: {'c': {}}}; a.b[x()].d = function $() {};");
    assertMapping("$", "a.b[x()].d");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToGetElem
  public void testAssignmentToGetElem() {
    test("function f() { win['x' + this.id] = function(a){}; }",
         "function f() { win['x' + this.id] = function $(a){}; }");

    
    assertMapping("$", "win[\"x\"+this.id]");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testGetElemWithDashes
  public void testGetElemWithDashes() {
    test("var foo = {}; foo['-'] = function() {};",
         "var foo = {}; foo['-'] = function $() {};");
    assertMapping("$", "foo[\"-\"]");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testDuplicateNames
  public void testDuplicateNames() {
    test("var a = function() { return 1; };a = function() { return 2; }",
         "var a = function $() { return 1; };a = function $() { return 2; }");
    assertMapping("$", "a");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testSimpleVarAssignment
  public void testSimpleVarAssignment() {
    test("var a = function() { return 1; }",
         "var a = function $a$() { return 1; }");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToProperty
  public void testAssignmentToProperty() {
    test("var a = {}; a.b = function() { return 1; }",
         "var a = {}; a.b = function $a$b$() { return 1; }");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype
  public void testAssignmentToPrototype() {
    test("function a() {} a.prototype.b = function() { return 1; };",
         "function a() {} " +
         "a.prototype.b = function $a$$b$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype2
  public void testAssignmentToPrototype2() {
    test("var a = {}; " +
         "a.b = function() {}; " +
         "a.b.prototype.c = function() { return 1; };",
         "var a = {}; " +
         "a.b = function $a$b$() {}; " +
         "a.b.prototype.c = function $a$b$$c$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype3
  public void testAssignmentToPrototype3() {
    test("function a() {} a.prototype['b'] = function() { return 1; };",
         "function a() {} " +
         "a.prototype['b'] = function $a$$b$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype4
  public void testAssignmentToPrototype4() {
    test("function a() {} a['prototype']['b'] = function() { return 1; };",
         "function a() {} " +
         "a['prototype']['b'] = function $a$$b$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testPrototypeInitializer
  public void testPrototypeInitializer() {
    test("function a(){} a.prototype = {b: function() { return 1; }};",
         "function a(){} " +
         "a.prototype = {b: function $a$$b$() { return 1; }};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testMultiplePrototypeInitializer
  public void testMultiplePrototypeInitializer() {
    test("function a(){} a.prototype = {b: function() { return 1; }, " +
         "c: function() { return 2; }};",
         "function a(){} " +
         "a.prototype = {b: function $a$$b$() { return 1; }," +
         "c: function $a$$c$() { return 2; }};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testRecursiveObjectLiteral
  public void testRecursiveObjectLiteral() {
    test("function a(){} a.prototype = {b: {c: function() { return 1; }}}",
         "function a(){}a.prototype={b:{c:function $a$$b$c$(){return 1}}}");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPropertyOfCallReturnValue
  public void testAssignmentToPropertyOfCallReturnValue() {
    test("document.getElementById('x').onClick = function() {};",
         "document.getElementById('x').onClick = " +
         "function $document$getElementById$onClick$() {};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPropertyOfArrayElement
  public void testAssignmentToPropertyOfArrayElement() {
    test("var a = {}; a.b = [{}]; a.b[0].c = function() {};",
         "var a = {}; a.b = [{}]; a.b[0].c = function $a$b$0$c$() {};");
    test("var a = {b: {'c': {}}}; a.b['c'].d = function() {};",
         "var a = {b: {'c': {}}}; a.b['c'].d = function $a$b$c$d$() {};");
    test("var a = {b: {'c': {}}}; a.b[x()].d = function() {};",
         "var a = {b: {'c': {}}}; a.b[x()].d = function $a$b$x$d$() {};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToGetElem
  public void testAssignmentToGetElem() {
    test("function f() {win['x' + this.id] = function(a){};}",
         "function f() {win['x' + this.id] = function $win$x$this$id$(a){};}");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testGetElemWithDashes
  public void testGetElemWithDashes() {
    test("var foo = {}; foo['-'] = function() {};",
         "var foo = {}; foo['-'] = function $foo$__0$() {};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testWhatCausedIeToFail
  public void testWhatCausedIeToFail() {
    
    
    
    test("var main;" +
        "(function() {" +
        "  main = function() {" +
        "    return 5;" +
        "  };" +
        "})();" +
        "" +
        "main();",
        "var main;(function(){main=function $main$(){return 5}})();main()");
  }

// com.google.javascript.jscomp.NormalizeTest::testSplitVar
  public void testSplitVar() {
    testSame("var a");
    test("var a, b",
         "var a; var b");
    test("var a, b, c",
         "var a; var b; var c");
    testSame("var a = 0 ");
    test("var a = 0 , b = foo()",
         "var a = 0; var b = foo()");
    test("var a = 0, b = 1, c = 2",
         "var a = 0; var b = 1; var c = 2");
    test("var a = foo(1), b = foo(2), c = foo(3)",
         "var a = foo(1); var b = foo(2); var c = foo(3)");

    
    test("for(var a = 0, b = foo(1), c = 1; c < b; c++) foo(2)",
         "var a = 0; var b = foo(1); var c = 1; for(; c < b; c++) foo(2)");

    
    test("for(;;) var b = foo(1), c = foo(2);",
        "for(;;){var b = foo(1); var c = foo(2)}");
    test("for(;;){var b = foo(1), c = foo(2);}",
         "for(;;){var b = foo(1); var c = foo(2)}");

    test("try{var b = foo(1), c = foo(2);} finally foo(3);",
         "try{var b = foo(1); var c = foo(2)} finally foo(3);");
    test("try{var b = foo(1),c = foo(2);} finally;",
         "try{var b = foo(1); var c = foo(2)} finally;");
    test("try{foo(0);} finally var b = foo(1), c = foo(2);",
         "try{foo(0);} finally {var b = foo(1); var c = foo(2)}");

    test("switch(a) {default: var b = foo(1), c = foo(2); break;}",
         "switch(a) {default: var b = foo(1); var c = foo(2); break;}");

    test("do var a = foo(1), b; while(false);",
         "do{var a = foo(1); var b} while(false);");
    test("a:var a,b,c;",
         "a:{ var a;var b; var c; }");
    test("a:for(var a,b,c;;);",
         "var a;var b; var c;a:for(;;);");
    test("if (true) a:var a,b;",
         "if (true)a:{ var a; var b; }");
  }

// com.google.javascript.jscomp.NormalizeTest::testDuplicateVarInExterns
  public void testDuplicateVarInExterns() {
    test("var extern;",
         " var extern = 3;", "var extern = 3;",
         null, null);
  }

// com.google.javascript.jscomp.NormalizeTest::testUnhandled
  public void testUnhandled() {
    testSame("var x = y = 1");
  }

// com.google.javascript.jscomp.NormalizeTest::testFor
  public void testFor() {
    
    test("for(a = 0; a < 2 ; a++) foo();",
         "a = 0; for(; a < 2 ; a++) foo()");
    
    test("for(var a = 0; c < b ; c++) foo()",
         "var a = 0; for(; c < b ; c++) foo()");

    
    test("a:for(var a = 0; c < b ; c++) foo()",
         "var a = 0; a:for(; c < b ; c++) foo()");
    
    test("a:b:for(var a = 0; c < b ; c++) foo()",
         "var a = 0; a:b:for(; c < b ; c++) foo()");

    
    test("if(x) for(var a = 0; c < b ; c++) foo()",
         "if(x){var a = 0; for(; c < b ; c++) foo()}");

    
    test("for(init(); a < 2 ; a++) foo();",
         "init(); for(; a < 2 ; a++) foo()");
  }

// com.google.javascript.jscomp.NormalizeTest::testForIn1
  public void testForIn1() {
    
    testSame("for(a in b) foo();");

    
    test("for(var a in b) foo()",
         "var a; for(a in b) foo()");

    
    test("a:for(var a in b) foo()",
         "var a; a:for(a in b) foo()");
    
    test("a:b:for(var a in b) foo()",
         "var a; a:b:for(a in b) foo()");

    
    test("if (x) for(var a in b) foo()",
         "if (x) { var a; for(a in b) foo() }");
  }

// com.google.javascript.jscomp.NormalizeTest::testForIn2
  public void testForIn2() {
    
    test("for(var a = foo() in b) foo()",
         "var a = foo(); for(a in b) foo()");
  }

// com.google.javascript.jscomp.NormalizeTest::testWhile
  public void testWhile() {
    
    test("while(c < b) foo()",
         "for(; c < b;) foo()");
  }

// com.google.javascript.jscomp.NormalizeTest::testMoveFunctions1
  public void testMoveFunctions1() throws Exception {
    test("function f() { if (x) return; foo(); function foo() {} }",
         "function f() {function foo() {} if (x) return; foo(); }");
    test("function f() { " +
            "function foo() {} " +
            "if (x) return;" +
            "foo(); " +
            "function bar() {} " +
         "}",
         "function f() {" +
           "function foo() {}" +
           "function bar() {}" +
           "if (x) return;" +
           "foo();" +
         "}");
  }

// com.google.javascript.jscomp.NormalizeTest::testMoveFunctions2
  public void testMoveFunctions2() throws Exception {
    testSame("function f() { function foo() {} }");
    test("function f() { f(); a:function bar() {} }",
         "function f() { f(); a:{ var bar = function () {} }}");
    test("function f() { f(); {function bar() {}}}",
         "function f() { f(); {var bar = function () {}}}");
    test("function f() { f(); if (true) {function bar() {}}}",
         "function f() { f(); if (true) {var bar = function () {}}}");
  }

// com.google.javascript.jscomp.NormalizeTest::testNormalizeFunctionDeclarations
  public void testNormalizeFunctionDeclarations() throws Exception {
    testSame("function f() {}");
    testSame("var f = function () {}");
    test("var f = function f() {}",
         "var f = function f$$1() {}");
    testSame("var f = function g() {}");
    test("a:function g() {}",
         "a:{ var g = function () {} }");
    test("{function g() {}}",
         "{var g = function () {}}");
    testSame("if (function g() {}) {}");
    test("if (true) {function g() {}}",
         "if (true) {var g = function () {}}");
    test("if (true) {} else {function g() {}}",
         "if (true) {} else {var g = function () {}}");
    testSame("switch (function g() {}) {}");
    test("switch (1) { case 1: function g() {}}",
         "switch (1) { case 1: var g = function () {}}");

    testSameInFunction("function f() {}");
    testInFunction("f(); a:function g() {}",
                   "f(); a:{ var g = function () {} }");
    testInFunction("f(); {function g() {}}",
                   "f(); {var g = function () {}}");
    testInFunction("f(); if (true) {function g() {}}",
                   "f(); if (true) {var g = function () {}}");
    testInFunction("if (true) {} else {function g() {}}",
                   "if (true) {} else {var g = function () {}}");
  }

// com.google.javascript.jscomp.NormalizeTest::testMakeLocalNamesUnique
  public void testMakeLocalNamesUnique() {
    if (!Normalize.MAKE_LOCAL_NAMES_UNIQUE) {
      return;
    }

    
    testSame("var a;");

    
    testSame("a;");

    
    test("var a;function foo(a){var b;a}",
         "var a;function foo(a$$1){var b;a$$1}");
    test("var a;function foo(){var b;a}function boo(){var b;a}",
         "var a;function foo(){var b;a}function boo(){var b$$1;a}");
    test("function foo(a){var b}" +
         "function boo(a){var b}",
         "function foo(a){var b}" +
         "function boo(a$$1){var b$$1}");

    
    test("var a = function foo(){foo()};var b = function foo(){foo()};",
         "var a = function foo(){foo()};var b = function foo$$1(){foo$$1()};");

    
    test("try { } catch(e) {e;}",
         "try { } catch(e) {e;}");
    test("try { } catch(e) {e;}; try { } catch(e) {e;}",
         "try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}");
    test("try { } catch(e) {e; try { } catch(e) {e;}};",
         "try { } catch(e) {e; try { } catch(e$$1) {e$$1;} }; ");

    
    test("\nvar window;", "var window;");

    
    test("\nvar window;" +
         "\nvar window;", "var window;");

    
    test("function f() {var window}",
         "function f() {var window$$1}");
  }

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations1
  public void testRemoveDuplicateVarDeclarations1() {
    test("function f() { var a; var a }",
         "function f() { var a; }");
    test("function f() { var a = 1; var a = 2 }",
         "function f() { var a = 1; a = 2 }");
    test("var a = 1; function f(){ var a = 2 }",
         "var a = 1; function f(){ var a$$1 = 2 }");
    test("function f() { var a = 1; lable1:var a = 2 }",
         "function f() { var a = 1; lable1:{a = 2}}");
    test("function f() { var a = 1; lable1:var a }",
         "function f() { var a = 1; lable1:{} }");
    test("function f() { var a = 1; for(var a in b); }",
         "function f() { var a = 1; for(a in b); }");
  }

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations2
  public void testRemoveDuplicateVarDeclarations2() {
    test("var e = 1; function f(){ try {} catch (e) {} var e = 2 }",
         "var e = 1; function f(){ try {} catch (e$$2) {} var e$$1 = 2 }");
  }

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations3
  public void testRemoveDuplicateVarDeclarations3() {
    test("var f = 1; function f(){}",
         "f = 1; function f(){}");
    test("var f; function f(){}",
         "function f(){}");
    test("if (a) { var f = 1; } else { function f(){} }",
         "if (a) { var f = 1; } else { f = function (){} }");

    test("function f(){} var f = 1;",
         "function f(){} f = 1;");
    test("function f(){} var f;",
         "function f(){}");
    test("if (a) { function f(){} } else { var f = 1; }",
         "if (a) { var f = function (){} } else { f = 1; }");

    
    
    test("function f(){} function f(){}",
         "function f(){} function f(){}",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
    test("if (a) { function f(){} } else { function f(){} }",
         "if (a) { var f = function (){} } else { f = function (){} }");
  }

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstants
  public void testRenamingConstants() {
    test("var ACONST = 4;var b = ACONST;",
         "var ACONST = 4; var b = ACONST;");

    test("var a, ACONST = 4;var b = ACONST;",
         "var a; var ACONST = 4; var b = ACONST;");

    test("var ACONST; ACONST = 4; var b = ACONST;",
         "var ACONST; ACONST = 4;" +
         "var b = ACONST;");

    test("var ACONST = new Foo(); var b = ACONST;",
         "var ACONST = new Foo(); var b = ACONST;");

    test("var aa; aa=1;", "var aa;aa=1");
  }

// com.google.javascript.jscomp.NormalizeTest::testSkipRenamingExterns
  public void testSkipRenamingExterns() {
    test("var EXTERN; var ext; ext.FOO;", "var b = EXTERN; var c = ext.FOO",
         "var b = EXTERN; var c = ext.FOO", null, null);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166a
  public void testIssue166a() {
    test("try { throw 1 } catch(e) {  var e=2 }",
         "try { throw 1 } catch(e) { var e=2 }",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166b
  public void testIssue166b() {
    test("function a() {" +
         "try { throw 1 } catch(e) {  var e=2 }" +
         "};",
         "function a() {" +
         "try { throw 1 } catch(e) { var e=2 }" +
         "}",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166c
  public void testIssue166c() {
    test("var e = 0; try { throw 1 } catch(e) {" +
             " var e=2 }",
         "var e = 0; try { throw 1 } catch(e) { var e=2 }",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166d
  public void testIssue166d() {
    test("function a() {" +
         "var e = 0; try { throw 1 } catch(e) {" +
             " var e=2 }" +
         "};",
         "function a() {" +
         "var e = 0; try { throw 1 } catch(e) { var e=2 }" +
         "}",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166e
  public void testIssue166e() {
    test("var e = 2; try { throw 1 } catch(e) {}",
         "var e = 2; try { throw 1 } catch(e$$1) {}");
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166f
  public void testIssue166f() {
    test("function a() {" +
         "var e = 2; try { throw 1 } catch(e) {}" +
         "}",
         "function a() {" +
         "var e = 2; try { throw 1 } catch(e$$1) {}" +
         "}");
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue
  public void testIssue() {
    super.allowExternsChanges(true);
    test("var a,b,c; var a,b", "a(), b()", "a(), b()", null, null);
  }

// com.google.javascript.jscomp.NormalizeTest::testNormalizeSyntheticCode
  public void testNormalizeSyntheticCode() {
    Compiler compiler = new Compiler();
    compiler.init(
        Lists.<JSSourceFile>newArrayList(),
        Lists.<JSSourceFile>newArrayList(), new CompilerOptions());
    Node code = Normalize.parseAndNormalizeSyntheticCode(
        compiler, "function f(x) {} function g(x) {}", "prefix_");
    assertEquals(
        "function f(x$$prefix_0){}function g(x$$prefix_1){}",
        compiler.toSource(code));
  }

// com.google.javascript.jscomp.NormalizeTest::testIsConstant
  public void testIsConstant() throws Exception {
    testSame("var CONST = 3; var b = CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testPropertyIsConstant1
  public void testPropertyIsConstant1() throws Exception {
    testSame("var a = {};a.CONST = 3; var b = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testPropertyIsConstant2
  public void testPropertyIsConstant2() throws Exception {
    testSame("var a = {CONST: 3}; var b = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testGetterPropertyIsConstant
  public void testGetterPropertyIsConstant() throws Exception {
    testSame("var a = { get CONST() {return 3} }; " +
             "var b = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testSetterPropertyIsConstant
  public void testSetterPropertyIsConstant() throws Exception {
    
    testSame("var a = { set CONST(b) {throw 'invalid'} }; " +
             "var c = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstantProperties
  public void testRenamingConstantProperties() {
    
    
    
    new WithCollapse().testConstantProperties();
  }

// com.google.javascript.jscomp.ObjectPropertyStringPostprocessTest::testFooDotBar
  public void testFooDotBar() {
    testPass("goog.global, foo.bar", "foo, 'bar'");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPostprocessTest::testFooGetElemBar
  public void testFooGetElemBar() {
    testPass("goog.global, foo[bar]", "foo, bar");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPostprocessTest::testFooBar
  public void testFooBar() {
    testPass("goog.global, foo$bar", "goog.global, 'foo$bar'");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testDeclaration
  public void testDeclaration() {
    test("goog.testing.ObjectPropertyString = function() {}",
         "JSCompiler_ObjectPropertyString = function() {}");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testFooBar
  public void testFooBar() {
    test("new goog.testing.ObjectPropertyString(foo, 'bar')",
         "new JSCompiler_ObjectPropertyString(goog.global, foo.bar)");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testFooPrototypeBar
  public void testFooPrototypeBar() {
    test("new goog.testing.ObjectPropertyString(foo.prototype, 'bar')",
         "new JSCompiler_ObjectPropertyString(goog.global, " +
         "foo.prototype.bar)");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testInvalidNumArgumentsError
  public void testInvalidNumArgumentsError() {
    testSame(new String[] {"new goog.testing.ObjectPropertyString()"},
        ObjectPropertyStringPreprocess.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testQualifedNameExpectedError
  public void testQualifedNameExpectedError() {
    testSame(
        new String[] {
          "new goog.testing.ObjectPropertyString(foo[a], 'bar')"
        },
        ObjectPropertyStringPreprocess.QUALIFIED_NAME_EXPECTED_ERROR);
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testStringLiteralExpectedError
  public void testStringLiteralExpectedError() {
    testSame(new String[] {"new goog.testing.ObjectPropertyString(foo, bar)"},
        ObjectPropertyStringPreprocess.STRING_LITERAL_EXPECTED_ERROR);
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testNoFix
  public void testNoFix() {
    testSame("x = x");
    testSame("x = x = x");
    testSame("x = x = x(x)");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testFix
  public void testFix() {
    test("       var a,b,x; x = a[x] = b[x]",
         "var c; var a,b,x; c = a[x] = b[x], x = c");
    test("       var a,b,x; x = a[1] = x.b",
         "var c; var a,b,x; c = a[1] = x.b, x = c");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testCombinedFix
  public void testCombinedFix() {
    test("       var a,b,c, x; x = a[x] = b[x] = c[x]",
         "var d; var a,b,c, x; d = a[x] = b[x] = c[x], x = d");
    test("       var a,b,c, x; x = a[1] = b[1] = x[1]",
         "var d; var a,b,c, x; d = a[1] = b[1] = x[1], x = d");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testNestedFix1
  public void testNestedFix1() {
    test("            var a,b,c,x,y;y= x = a[x] = b[y] = c[x];",
         "var e;var d;var a,b,c,x,y;d=(e = a[x] = b[y] = c[x], x=e), y=d;");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testNestedFix2
  public void testNestedFix2() {
    test("            var a,b,c,x,y;y=a[x]= x=a[x]=b[y]=c[x];",
         "var e;var d;var a,b,c,x,y;d=a[x]=(e=a[x]=b[y]=c[x], x=e), y=d;");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testJqueryTest
  public void testJqueryTest() {
    test("       z = bar[z] = bar[z] || [];",
         "var a; a = bar[z] = bar[z] || [], z=a");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testNoCrossingScope
  public void testNoCrossingScope() {
    testSame("x = function(x) { return a[x] + b[x] }");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testForLoops
  public void testForLoops() {
    test("       var a,b,x;for(x = a[x] = b[x];;)        {}",
         "var c; var a,b,x;for(c = a[x] = b[x], x = c;;) {}");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testForInLoops
  public void testForInLoops() {
    test("       var a,b,x;for(var j in  x = a[x] = b[x])         {}",
         "var c; var a,b,x;for(var j in (c = a[x] = b[x], x = c)) {}");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testUsedInCondition
  public void testUsedInCondition() {
    test("       var a,b,x;if(x = a[x] = b[x]) {}",
         "var c; var a,b,x;if((c = a[x] = b[x], x = c)) {}");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testUsedInExpression
  public void testUsedInExpression() {
    test("       var a,b,x; FOO( x = a[x] = b[x]);",
         "var c; var a,b,x; FOO((c = a[x] = b[x], x = c));");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testLocalScope
  public void testLocalScope() {
    test("function FOO() {       var a,b,x; x = a[x] = b[x]}",
         "function FOO() {var c; var a,b,x; c = a[x] = b[x], x = c}");
    test("function FOO() {       var a,b,x; x = a[1] = x.b}",
         "function FOO() {var c; var a,b,x; c = a[1] = x.b, x = c}");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testProperNames1
  public void testProperNames1() {
    test("var a,b,c,d,x;" +
         "function f() {" +
         "  function g() { return a }" +
         "  x = a[x] = b[x];" +
         "  return g();" +
         "}",

         "var a,b,c,d,x;" +
         "function f() {" +
         "  var e;" +
         "  function g() { return a }" +
         "  e = a[x] = b[x], x = e;" +
         "  return g();" +
         "}");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testProperNames2
  public void testProperNames2() {
    test("var a;",
         "function f() {" +
         " var b,x; x = a[x] = b[x];" +
         " return g();" +
         "}",

         "function f() {" +
         " var c;" +
         " var b,x; c = a[x] = b[x], x = c;" +
         " return g();" +
         "}", null, null);
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testSaveShadowing
  public void testSaveShadowing() {
    
    test("       var a,b,x; x = a[x] = b[x];" +
         "function FOO() {       var a,b,x; x = a[x] = b[x]}",

         "var c; var a,b,x; c = a[x] = b[x], x = c;" +
         "function FOO() {var c; var a,b,x; c = a[x] = b[x], x = c}");

  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testSimple
  public void testSimple() {
    test("function foo()   { alert(arguments[0]); }",
         "function foo(p0) { alert(p0); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoVarArgs
  public void testNoVarArgs() {
    testSame("function f(a,b,c) { alert(a + b + c) }");

    test("function f(a,b,c) { alert(arguments[0]) }",
         "function f(a,b,c) { alert(a) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testMissingVarArgs
  public void testMissingVarArgs() {
    testSame("function f() { alert(arguments[x]) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testArgumentRefOnNamedParameter
  public void testArgumentRefOnNamedParameter() {
    test("function f(a,b) { alert(arguments[0]) }",
         "function f(a,b) { alert(a) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoVarArgs
  public void testTwoVarArgs() {
    test("function foo(a) { alert(arguments[1] + arguments[2]); }",
         "function foo(a, p0, p1) { alert(p0 + p1); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoFourArgsTwoUsed
  public void testTwoFourArgsTwoUsed() {
    test("function foo() { alert(arguments[0] + arguments[3]); }",
         "function foo(p0, p1, p2, p3) { alert(p0 + p3); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testOneRequired
  public void testOneRequired() {
    test("function foo(req0, var_args) { alert(req0 + arguments[1]); }",
         "function foo(req0, var_args) { alert(req0 + var_args); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoRequiredSixthVarArgReferenced
  public void testTwoRequiredSixthVarArgReferenced() {
    test("function foo(r0, r1, var_args) {alert(r0 + r1 + arguments[5]);}",
         "function foo(r0, r1, var_args, p0, p1, p2) { alert(r0 + r1 + p2); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoRequiredOneOptionalFifthVarArgReferenced
  public void testTwoRequiredOneOptionalFifthVarArgReferenced() {
    test("function foo(r0, r1, opt_1)"
       + "  {alert(r0 + r1 + opt_1 + arguments[4]);}",
         "function foo(r0, r1, opt_1, p0, p1)"
       + "  {alert(r0 + r1 + opt_1 + p1); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoRequiredTwoOptionalSixthVarArgReferenced
  public void testTwoRequiredTwoOptionalSixthVarArgReferenced() {
    test("function foo(r0, r1, opt_1, opt_2)"
       + "  {alert(r0 + r1 + opt_1 + opt_2 + arguments[5]);}",
         "function foo(r0, r1, opt_1, opt_2, p0, p1)"
       + "  {alert(r0 + r1 + opt_1 + opt_2 + p1); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctions
  public void testInnerFunctions() {
    test("function f() { function b(  ) { arguments[0]  }}",
         "function f() { function b(p0) {            p0 }}");

    test("function f(  ) { function b() { }  arguments[0] }",
         "function f(p0) { function b() { }            p0 }");

    test("function f( )  { arguments[0]; function b(  ) { arguments[0] }}",
         "function f(p1) {           p1; function b(p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsWithNamedArgumentInInnerFunction
  public void testInnerFunctionsWithNamedArgumentInInnerFunction() {
    test("function f() { function b(x   ) { arguments[1] }}",
         "function f() { function b(x,p0) {           p0 }}");

    test("function f(  ) { function b(x) { }  arguments[0] }",
         "function f(p0) { function b(x) { }            p0 }");

    test("function f( )  { arguments[0]; function b(x   ) { arguments[1] }}",
         "function f(p1) {           p1; function b(x,p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsWithNamedArgumentInOutterFunction
  public void testInnerFunctionsWithNamedArgumentInOutterFunction() {
    test("function f(x) { function b(  ) { arguments[0] }}",
         "function f(x) { function b(p0) {           p0 }}");

    test("function f(x   ) { function b() { }  arguments[1] }",
         "function f(x,p0) { function b() { }            p0 }");

    test("function f(x   ) { arguments[1]; function b(  ) { arguments[0] }}",
         "function f(x,p1) {           p1; function b(p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsWithNamedArgumentInInnerAndOutterFunction
  public void testInnerFunctionsWithNamedArgumentInInnerAndOutterFunction() {
    test("function f(x) { function b(x   ) { arguments[1] }}",
         "function f(x) { function b(x,p0) {           p0 }}");

    test("function f(x   ) { function b(x) { }  arguments[1] }",
         "function f(x,p0) { function b(x) { }            p0 }");

    test("function f(x   ) { arguments[1]; function b(x   ) { arguments[1] }}",
         "function f(x,p1) {           p1; function b(x,p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsAfterArguments
  public void testInnerFunctionsAfterArguments() {
    
    
    test("function f(  ) { arguments[0]; function b() { function c() { }} }",
         "function f(p0) {           p0; function b() { function c() { }} }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoOptimizationWhenGetProp
  public void testNoOptimizationWhenGetProp() {
    testSame("function f() { arguments[0]; arguments.size }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoOptimizationWhenIndexIsNotNumberConstant
  public void testNoOptimizationWhenIndexIsNotNumberConstant() {
    testSame("function f() { arguments[0]; arguments['callee'].length}");
    testSame("function f() { arguments[0]; arguments.callee.length}");
    testSame(
        "function f() { arguments[0]; var x = 'callee'; arguments[x].length}");
  }
