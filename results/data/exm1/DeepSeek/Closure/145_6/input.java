// buggy code
  private boolean isOneExactlyFunctionOrDo(Node n) {
        // For labels with block children, we need to ensure that a
        // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
        // are skipped. 
          // Either a empty statement or an block with more than one child,
          // way it isn't a FUNCTION or DO.
      return (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }

// relevant test
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

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign1
  public void testNestedAssign1() {
    test("var a, b = a = 1, c = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign2
  public void testNestedAssign2() {
    testSame("var a, b = a = 1; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign3
  public void testNestedAssign3() {
    testSame("var a, b = a = 1; a = b = 2; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign4
  public void testNestedAssign4() {
    testSame("var a, b = a = 1; b = a = 2; foo(b)");
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
    test("function() { win['x' + this.id] = function(a){}; }",
         "function() { win['x' + this.id] = function $(a){}; }");

    
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
    test("function() { win['x' + this.id] = function(a){}; }",
         "function() { win['x' + this.id] = function $win$x$this$id$(a){}; }");
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

    
    testSame("\nvar window;");

    
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

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testSimple
  public void testSimple() {
    test("function foo()   { alert(arguments[0]); }",
         "function foo(p0) { alert(p0); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoVarArgs
  public void testNoVarArgs() {
    testSame("function(a,b,c) { alert(a + b + c) }");

    test("function(a,b,c) { alert(arguments[0]) }",
         "function(a,b,c) { alert(a) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testMissingVarArgs
  public void testMissingVarArgs() {
    testSame("function() { alert(arguments[x]) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testArgumentRefOnNamedParameter
  public void testArgumentRefOnNamedParameter() {
    test("function(a,b) { alert(arguments[0]) }",
         "function(a,b) { alert(a) }");
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

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoOptimizationWhenArgumentIsUsedAsFunctionCall
  public void testNoOptimizationWhenArgumentIsUsedAsFunctionCall() {
    testSame("function f() {arguments[0]()}");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoRemoval
  public void testNoRemoval() {
    testSame("function foo(p1) { } foo(1); foo(2)");
    testSame("function foo(p1) { } foo(1,2); foo(3,4)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNotAFunction
  public void testNotAFunction() {
    testSame("var x = 1; x; x = 2");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalNamedFunction
  public void testRemoveOneOptionalNamedFunction() {
    test("function foo(p1) { } foo()", "function foo() {var p1} foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalVarAssignment
  public void testRemoveOneOptionalVarAssignment() {
    test("var foo = function (p1) { }; foo()",
        "var foo = function () {var p1}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalExpressionAssign
  public void testRemoveOneOptionalExpressionAssign() {
    test("var foo; foo = function (p1) { }; foo()",
        "var foo; foo = function () {var p1}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalOneRequired
  public void testRemoveOneOptionalOneRequired() {
    test("function foo(p1, p2) { } foo(1); foo(2)",
        "function foo(p1) {var p2} foo(1); foo(2)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalMultipleCalls
  public void testRemoveOneOptionalMultipleCalls() {
    test( "function foo(p1, p2) { } foo(1); foo(2); foo()",
        "function foo(p1) {var p2} foo(1); foo(2); foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalMultiplePossibleDefinition
  public void testRemoveOneOptionalMultiplePossibleDefinition() {
    String src = "var goog = {};" +
        "goog.foo = function (p1, p2) { };" +
        "goog.foo = function (q1, q2) { };" +
        "goog.foo = function (r1, r2) { };" +
        "goog.foo(1); goog.foo(2); goog.foo()";

    String expected = "var goog = {};" +
        "goog.foo = function (p1) { var p2 };" +
        "goog.foo = function (q1) { var q2 };" +
        "goog.foo = function (r1) { var r2 };" +
        "goog.foo(1); goog.foo(2); goog.foo()";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveTwoOptionalMultiplePossibleDefinition
  public void testRemoveTwoOptionalMultiplePossibleDefinition() {
    String src = "var goog = {};" +
        "goog.foo = function (p1, p2, p3, p4) { };" +
        "goog.foo = function (q1, q2, q3, q4) { };" +
        "goog.foo = function (r1, r2, r3, r4) { };" +
        "goog.foo(1,0); goog.foo(2,1); goog.foo()";

    String expected = "var goog = {};" +
        "goog.foo = function(p1, p2) { var p4; var p3};" +
        "goog.foo = function(q1, q2) { var q4; var q3};" +
        "goog.foo = function(r1, r2) { var r4; var r3};" +
        "goog.foo(1,0); goog.foo(2,1); goog.foo()";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstructorOptArgsNotRemoved
  public void testConstructorOptArgsNotRemoved() {
    String src =
        "" +
        "var goog = function(){};" +
        "goog.prototype.foo = function(a,b) {};" +
        "goog.prototype.bar = function(a) {};" +
        "goog.bar.inherits(goog.foo);" +
        "new goog.foo(2,3);" +
        "new goog.foo(1,2);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testUnknown
  public void testUnknown() {
    String src = "var goog1 = {};" +
        "goog1.foo = function () { };" +
        "var goog2 = {};" +
        "goog2.foo = function (p1) { };" +
        "var x = getGoog();" +
        "x.foo()";

    String expected = "var goog1 = {};" +
        "goog1.foo = function () { };" +
        "var goog2 = {};" +
        "goog2.foo = function () { var p1 };" +
        "var x = getGoog();" +
        "x.foo()";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveVarArg
  public void testRemoveVarArg() {
    test("function foo(p1, var_args) { } foo(1); foo(2)",
        "function foo(p1) { var var_args } foo(1); foo(2)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize
  public void testAliasMethodsDontGetOptimize() {
    String src =
        "var foo = function(a, b) {};" +
        "var goog = {};" +
        "goog.foo = foo;" +
        "goog.prototype.bar = goog.foo;" +
        "new goog().bar(1,2);" +
        "foo(2);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize2
  public void testAliasMethodsDontGetOptimize2() {
    String src =
        "var foo = function(a, b) {};" +
        "var bar = foo;" +
        "foo(1);" +
        "bar(2,3);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize3
  public void testAliasMethodsDontGetOptimize3() {
    String src =
        "var array = {};" +
        "array[0] = function(a, b) {};" +
        "var foo = array[0];" + 
        "foo(1);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize4
  public void testAliasMethodsDontGetOptimize4() {
    String src = "function foo(bar) {};" +
        "baz = function(a) {};" +
        "baz(1);" +
        "foo(baz);"; 
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveConstantArgument
  public void testRemoveConstantArgument() {
    
    test("function foo(p1, p2) {}; foo(1,2); foo(2,2);",
         "function foo(p1) {var p2 = 2}; foo(1); foo(2)");

    
    testSame("function foo(p1, p2) {}; foo(1); foo(2,3);");

    
    test("function foo(a,b,c){}; foo(1, 2, 3); foo(1, 2, 4); foo(2, 2, 3)",
         "function foo(a,c){var b=2}; foo(1, 3); foo(1, 4); foo(2, 3)");

    
    test("function foo(a) {}; foo(1); foo(1.0);",
         "function foo() {var a = 1;}; foo(); foo();");

    
    String src =
        "" +
        "function Person(){}; Person.prototype.run = function(a, b) {};" +
        "Person.run(1, 'a'); Person.run(2, 'a')";
    String expected =
        "function Person(){}; Person.prototype.run = " +
        "function(a) {var b = 'a'};" +
        "Person.run(1); Person.run(2)";
    test(src, expected);

  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCanDeleteArgumentsAtAnyPosition
  public void testCanDeleteArgumentsAtAnyPosition() {
    
    String src =
        "function foo(a,b,c,d,e) {};" +
        "foo(1,2,3,4,5);" +
        "foo(2,2,4,4,5);";
    String expected =
        "function foo(a,c) {var b=2; var d=4; var e=5;};" +
        "foo(1,3);" +
        "foo(2,4);";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoOptimizationForExternsFunctions
  public void testNoOptimizationForExternsFunctions() {
    testSame("function _foo(x, y, z){}; _foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoOptimizationForGoogExportSymbol
  public void testNoOptimizationForGoogExportSymbol() {
    testSame("goog.exportSymbol('foo', foo);" +
             "function foo(x, y, z){}; foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoArgumentRemovalNonEqualNodes
  public void testNoArgumentRemovalNonEqualNodes() {
    testSame("function foo(a){}; foo('bar'); foo('baz');");
    testSame("function foo(a){}; foo(1.0); foo(2.0);");
    testSame("function foo(a){}; foo(true); foo(false);");
    testSame("var a = 1, b = 2; function foo(a){}; foo(a); foo(b);");
    testSame("function foo(a){}; foo(/&/g); foo(/</g);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionPassedAsParam
  public void testFunctionPassedAsParam() {
    String src =
        " function person(){}; " +
        "person.prototype.run = function(a, b) {};" +
        "person.prototype.walk = function() {};" +
        "person.prototype.foo = function() { this.run(this.walk, 0.1)};" +
        "person.foo();";
    String expected =
        "function person(){}; person.prototype.run = function(a) {" +
        "  var b = 0.1;};" +
        "person.prototype.walk = function() {};" +
        "person.prototype.foo = function() { this.run(this.walk)};" +
        "person.foo();";

    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCallIsIgnore
  public void testCallIsIgnore() {
    testSame("var goog;" +
        "goog.foo = function(a, opt) {};" +
        "var bar = function(){goog.foo.call(this, 1)};" +
        "goog.foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testApplyIsIgnore
  public void testApplyIsIgnore() {
    testSame("var goog;" +
        "goog.foo = function(a, opt) {};" +
        "var bar = function(){goog.foo.apply(this, 1)};" +
        "goog.foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionWithReferenceToArgumentsShouldNotBeOptimize
  public void testFunctionWithReferenceToArgumentsShouldNotBeOptimize() {
    testSame("function foo(a,b,c) { return arguments.size; };" +
             "foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstantArgumentsToConstructorCanBeOptimized
  public void testConstantArgumentsToConstructorCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo(1);";
    String expected = "function foo() {var a=1;};" +
        "var bar = new foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testOptionalArgumentsToConstructorCanBeOptimized
  public void testOptionalArgumentsToConstructorCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo();";
    String expected = "function foo() {var a;};" +
        "var bar = new foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRegexesCanBeInlined
  public void testRegexesCanBeInlined() {
    test("function foo(a) {}; foo(/abc/);",
        "function foo() {var a = /abc/}; foo();");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstructorUsedAsFunctionCanBeOptimized
  public void testConstructorUsedAsFunctionCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo(1);" +
        "foo(1);";
    String expected = "function foo() {var a=1;};" +
        "var bar = new foo();" +
        "foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeConstructorWhenArgumentsAreNotEqual
  public void testDoNotOptimizeConstructorWhenArgumentsAreNotEqual() {
    testSame("function Foo(a) {};" +
        "var bar = new Foo(1);" +
        "var baz = new Foo(2);");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testNoFunction
  public void testNoFunction() {
    replace("\"foo\"");
    replace("var foo");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testOneFunction
  public void testOneFunction() {
    replace("\"foo\";function foo(){\"foo\"}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testTwoFunctions
  public void testTwoFunctions() {
    replace("\"foo\";function f1(){\"foo\"}function f2(){\"foo\"}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testInnerFunctions
  public void testInnerFunctions() {
    replace("\"foo\";function f1(){\"foo\";function f2(){\"foo\"}}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testManyFunctions
  public void testManyFunctions() {
    StringBuffer sb = new StringBuffer("\"foo\";");
    for (int i = 0; i < 20; i++) {
      sb.append("function f");
      sb.append(i);
      sb.append("(){\"foo\"}");
    }
    replace(sb.toString());
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testEmptyPass
  public void testEmptyPass() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of();

    testSame("var x; var y;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationOrder
  public void testOptimizationOrder() {
    

    final List<String> visitationLog = Lists.newArrayList();

    AbstractPeepholeOptimization note1Applied = 
        new AbstractPeepholeOptimization() {
      @Override
      public Node optimizeSubtree(Node node) {
        if (node.getType() == Token.NAME) {
          visitationLog.add(node.getString() + "1");
        }

        return node;
      }
    };

    AbstractPeepholeOptimization note2Applied = 
        new AbstractPeepholeOptimization() {
      @Override
      public Node optimizeSubtree(Node node) {
        if (node.getType() == Token.NAME) {
          visitationLog.add(node.getString() + "2");
        }

        return node;
      }
    };

    currentPeepholePasses =
      ImmutableSet.<AbstractPeepholeOptimization>of(note1Applied, note2Applied);
    
    test("var x; var y", "var x; var y");

    

    assertEquals(4, visitationLog.size());
    assertEquals("x1", visitationLog.get(0));
    assertEquals("x2", visitationLog.get(1));
    assertEquals("y1", visitationLog.get(2));
    assertEquals("y2", visitationLog.get(3));
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationRemovingSubtreeChild
  public void testOptimizationRemovingSubtreeChild() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of(new 
          RemoveNodesNamedXUnderVarOptimization());
    
    test("var x,y;", "var y;");
    test("var y,x;", "var y;");
    test("var x,y,x;", "var y;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationRemovingSubtree
  public void testOptimizationRemovingSubtree() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of(new 
          RemoveNodesNamedXOptimization());
    
    test("var x,y;", "var y;");
    test("var y,x;", "var y;");
    test("var x,y,x;", "var y;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationRemovingSubtreeParent
  public void testOptimizationRemovingSubtreeParent() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of(new 
          RemoveParentVarsForNodesNamedX());
  
    test("var x; var y", "var y");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationsRemoveParentAfterRemoveChild
  public void testOptimizationsRemoveParentAfterRemoveChild() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of(
          new RemoveNodesNamedXOptimization(),
          new RemoveParentVarsForNodesNamedX());
    
    test("var x,y; var z;", "var y; var z;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationReplacingNode
  public void testOptimizationReplacingNode() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of(
          new RenameYToX(),
          new RemoveParentVarsForNodesNamedX());
    
    test("var y; var z;", "var z;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleProvides
  public void testSimpleProvides() {
    test("goog.provide('foo');",
         "var foo={};");
    test("goog.provide('foo.bar');",
         "var foo={}; foo.bar={};");
    test("goog.provide('foo.bar.baz');",
         "var foo={}; foo.bar={}; foo.bar.baz={};");
    test("goog.provide('foo.bar.baz.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.bar.baz.boo={};");
    test("goog.provide('goog.bar');",
         "goog.bar={};");  
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleProvides
  public void testMultipleProvides() {
    test("goog.provide('foo.bar'); goog.provide('foo.baz');",
         "var foo={}; foo.bar={}; foo.baz={};");
    test("goog.provide('foo.bar.baz'); goog.provide('foo.boo.foo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.boo={}; foo.boo.foo={};");
    test("goog.provide('foo.bar.baz'); goog.provide('foo.bar.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.bar.boo={};");
    test("goog.provide('foo.bar.baz'); goog.provide('goog.bar.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; goog.bar={}; " +
         "goog.bar.boo={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalOfProvidedObjLit
  public void testRemovalOfProvidedObjLit() {
    test("goog.provide('foo'); foo = 0;",
         "var foo = 0;");
    test("goog.provide('foo'); foo = {a: 0};",
         "var foo = {a: 0};");
    test("goog.provide('foo'); foo = function(){};",
         "var foo = function(){};");
    test("goog.provide('foo'); var foo = 0;",
         "var foo = 0;");
    test("goog.provide('foo'); var foo = {a: 0};",
         "var foo = {a: 0};");
    test("goog.provide('foo'); var foo = function(){};",
         "var foo = function(){};");
    test("goog.provide('foo.bar.Baz'); foo.bar.Baz=function(){};",
         "var foo={}; foo.bar={}; foo.bar.Baz=function(){};");
    test("goog.provide('foo.bar.moo'); foo.bar.moo={E:1,S:2};",
         "var foo={}; foo.bar={}; foo.bar.moo={E:1,S:2};");
    test("goog.provide('foo.bar.moo'); foo.bar.moo={E:1}; foo.bar.moo={E:2};",
         "var foo={}; foo.bar={}; foo.bar.moo={E:1}; foo.bar.moo={E:2};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvidedDeclaredFunctionError
  public void testProvidedDeclaredFunctionError() {
    test("goog.provide('foo'); function foo(){}",
         null, FUNCTION_NAMESPACE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment1
  public void testRemovalMultipleAssignment1() {
    test("goog.provide('foo'); foo = 0; foo = 1",
         "var foo = 0; foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment2
  public void testRemovalMultipleAssignment2() {
    test("goog.provide('foo'); var foo = 0; foo = 1",
         "var foo = 0; foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment3
  public void testRemovalMultipleAssignment3() {
    test("goog.provide('foo'); foo = 0; var foo = 1",
         "foo = 0; var foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment4
  public void testRemovalMultipleAssignment4() {
    test("goog.provide('foo.bar'); foo.bar = 0; foo.bar = 1",
         "var foo = {}; foo.bar = 0; foo.bar = 1");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoRemovalFunction1
  public void testNoRemovalFunction1() {
    test("goog.provide('foo'); function f(){foo = 0}",
         "var foo = {}; function f(){foo = 0}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoRemovalFunction2
  public void testNoRemovalFunction2() {
    test("goog.provide('foo'); function f(){var foo = 0}",
         "var foo = {}; function f(){var foo = 0}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf1
  public void testRemovalMultipleAssignmentInIf1() {
    test("goog.provide('foo'); if (true) { var foo = 0 } else { foo = 1 }",
         "if (true) { var foo = 0 } else { foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf2
  public void testRemovalMultipleAssignmentInIf2() {
    test("goog.provide('foo'); if (true) { foo = 0 } else { var foo = 1 }",
         "if (true) { foo = 0 } else { var foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf3
  public void testRemovalMultipleAssignmentInIf3() {
    test("goog.provide('foo'); if (true) { foo = 0 } else { foo = 1 }",
         "if (true) { var foo = 0 } else { foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf4
  public void testRemovalMultipleAssignmentInIf4() {
    test("goog.provide('foo.bar');" +
         "if (true) { foo.bar = 0 } else { foo.bar = 1 }",
         "var foo = {}; if (true) { foo.bar = 0 } else { foo.bar = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError1
  public void testMultipleDeclarationError1() {
    String rest = "if (true) { foo.bar = 0 } else { foo.bar = 1 }";
    test("goog.provide('foo.bar');" + "var foo = {};" + rest,
         "var foo = {};" + "var foo = {};" + rest);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError2
  public void testMultipleDeclarationError2() {
    test("goog.provide('foo.bar');" +
         "if (true) { var foo = {}; foo.bar = 0 } else { foo.bar = 1 }",
         "var foo = {};" +
         "if (true) {" +
         "  var foo = {}; foo.bar = 0" +
         "} else {" +
         "  foo.bar = 1" +
         "}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError3
  public void testMultipleDeclarationError3() {
    test("goog.provide('foo.bar');" +
         "if (true) { foo.bar = 0 } else { var foo = {}; foo.bar = 1 }",
         "var foo = {};" +
         "if (true) {" +
         "  foo.bar = 0" +
         "} else {" +
         "  var foo = {}; foo.bar = 1" +
         "}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideAfterDeclarationError
  public void testProvideAfterDeclarationError() {
    test("var x = 42; goog.provide('x');",
         "var x = 42; var x = {}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideErrorCases
  public void testProvideErrorCases() {
    test("goog.provide();", "", NULL_ARGUMENT_ERROR);
    test("goog.provide(5);", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide([]);", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide({});", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide('foo', 'bar');", "", TOO_MANY_ARGUMENTS_ERROR);
    test("goog.provide('foo'); goog.provide('foo');", "",
        DUPLICATE_NAMESPACE_ERROR);
    test("goog.provide('foo.bar'); goog.provide('foo'); goog.provide('foo');",
        "", DUPLICATE_NAMESPACE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalOfRequires
  public void testRemovalOfRequires() {
    test("goog.provide('foo'); goog.require('foo');",
         "var foo={};");
    test("goog.provide('foo.bar'); goog.require('foo.bar');",
         "var foo={}; foo.bar={};");
    test("goog.provide('foo.bar.baz'); goog.require('foo.bar.baz');",
         "var foo={}; foo.bar={}; foo.bar.baz={};");
    test("goog.provide('foo'); var x = 3; goog.require('foo'); something();",
         "var foo={}; var x = 3; something();");
    testSame("foo.require('foo.bar');");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireErrorCases
  public void testRequireErrorCases() {
    test("goog.require();", "", NULL_ARGUMENT_ERROR);
    test("goog.require(5);", "", INVALID_ARGUMENT_ERROR);
    test("goog.require([]);", "", INVALID_ARGUMENT_ERROR);
    test("goog.require({});", "", INVALID_ARGUMENT_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testLateProvides
  public void testLateProvides() {
    test("goog.require('foo'); goog.provide('foo');",
         "var foo={};", LATE_PROVIDE_ERROR);
    test("goog.require('foo.bar'); goog.provide('foo.bar');",
         "var foo={}; foo.bar={};", LATE_PROVIDE_ERROR);
    test("goog.provide('foo.bar'); goog.require('foo'); goog.provide('foo');",
         "var foo={}; foo.bar={};", LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMissingProvides
  public void testMissingProvides() {
    test("goog.require('foo');",
         "", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); goog.require('Foo');",
         "var foo={};", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); goog.require('foo.bar');",
         "var foo={};", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); var EXPERIMENT_FOO = true; " +
             "if (EXPERIMENT_FOO) {goog.require('foo.bar');}",
         "var foo={}; var EXPERIMENT_FOO = true; if (EXPERIMENT_FOO) {}",
         MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNewDateGoogNowSimplification
  public void testNewDateGoogNowSimplification() {
    test("var x = new Date(goog.now());", "var x = new Date();");
    testSame("var x = new Date(goog.now() + 1);");
    testSame("var x = new Date(goog.now(1));");
    testSame("var x = new Date(1, goog.now());");
    testSame("var x = new Date(1);");
    testSame("var x = new Date();");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testAddDependency
  public void testAddDependency() {
    test("goog.addDependency('x.js', ['A', 'B'], []);", "0");

    Compiler compiler = getLastCompiler();
    assertTrue(compiler.getTypeRegistry().isForwardDeclaredType("A"));
    assertTrue(compiler.getTypeRegistry().isForwardDeclaredType("B"));
    assertFalse(compiler.getTypeRegistry().isForwardDeclaredType("C"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidSetCssNameMapping
  public void testValidSetCssNameMapping() {
    test("goog.setCssNameMapping({foo:'bar',\"biz\":'baz'});", "");
    CssRenamingMap map = getLastCompiler().getCssRenamingMap();
    assertNotNull(map);
    assertEquals("bar", map.get("foo"));
    assertEquals("baz", map.get("biz"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSetCssNameMappingNonStringValueReturnsError
  public void testSetCssNameMappingNonStringValueReturnsError() {
    
    test("var BAR = {foo:'bar'}; goog.setCssNameMapping(BAR);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping([]);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping(false);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping(null);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping(undefined);", "",
        INVALID_ARGUMENT_ERROR);

    
    test("var BAR = 'bar'; goog.setCssNameMapping({foo:BAR});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:6});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:false});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:null});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:undefined});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testBadCrossModuleRequire
  public void testBadCrossModuleRequire() {
    test(
        createModuleStar(
            "",
            "goog.provide('goog.ui');",
            "goog.require('goog.ui');"),
        new String[] {
          "",
          "goog.ui = {};",
          ""
        },
        null,
        XMODULE_REQUIRE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testGoodCrossModuleRequire1
  public void testGoodCrossModuleRequire1() {
    test(
        createModuleStar(
            "goog.provide('goog.ui');",
            "",
            "goog.require('goog.ui');"),
        new String[] {
            "goog.ui = {};",
            "",
            "",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testGoodCrossModuleRequire2
  public void testGoodCrossModuleRequire2() {
    test(
        createModuleStar(
            "",
            "",
            "goog.provide('goog.ui'); goog.require('goog.ui');"),
        new String[] {
            "",
            "",
            "goog.ui = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleAdditionalProvide
  public void testSimpleAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleAdditionalProvideAtEnd
  public void testSimpleAdditionalProvideAtEnd() {
    additionalEndCode = "goog.provide('b.B'); b.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};var b={};b.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleDottedAdditionalProvide
  public void testSimpleDottedAdditionalProvide() {
    additionalCode = "goog.provide('a.b.B'); a.b.B = {};";
    test("goog.provide('c.d.D'); c.d.D = {};",
         "var a={};a.b={};a.b.B={};var c={};c.d={};c.d.D={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingAdditionalProvide
  public void testOverlappingAdditionalProvide() {
    additionalCode = "goog.provide('a.B'); a.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.B={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingAdditionalProvideAtEnd
  public void testOverlappingAdditionalProvideAtEnd() {
    additionalEndCode = "goog.provide('a.B'); a.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};a.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingDottedAdditionalProvide
  public void testOverlappingDottedAdditionalProvide() {
    additionalCode = "goog.provide('a.b.B'); a.b.B = {};";
    test("goog.provide('a.b.C'); a.b.C = {};",
         "var a={};a.b={};a.b.B={};a.b.C={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireOfAdditionalProvide
  public void testRequireOfAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.require('b.B'); goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMissingRequireWithAdditionalProvide
  public void testMissingRequireWithAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.require('b.C'); goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};",
         MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testLateRequire
  public void testLateRequire() {
    additionalEndCode = "goog.require('a.A');";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testReorderedProvides
  public void testReorderedProvides() {
    additionalCode = "a.B = {};";  
    addAdditionalNamespace = true;
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.B={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testReorderedProvides2
  public void testReorderedProvides2() {
    additionalEndCode = "a.B = {};";
    addAdditionalNamespace = true;
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};a.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder1
  public void testProvideOrder1() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    
    
    test("goog.provide('a.b');" +
         "goog.provide('a.b.c');" +
         "a.b.c;" +
         "a.b = function(x,y) {};",
         "var a = {};" +
         "a.b = {};" +
         "a.b.c = {};" +
         "a.b.c;" +
         "a.b = function(x,y) {};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder2
  public void testProvideOrder2() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    
    
    test("goog.provide('a.b');" +
         "goog.provide('a.b.c');" +
         "a.b = function(x,y) {};" +
         "a.b.c;",
         "var a = {};" +
         "a.b = {};" +
         "a.b.c = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3a
  public void testProvideOrder3a() {
    test("goog.provide('a.b');" +
         "a.b = function(x,y) {};" +
         "goog.provide('a.b.c');" +
         "a.b.c;",
         "var a = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c = {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3b
  public void testProvideOrder3b() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    test("goog.provide('a.b');" +
         "a.b = function(x,y) {};" +
         "goog.provide('a.b.c');" +
         "a.b.c;",
         "var a = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c = {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4a
  public void testProvideOrder4a() {
    test("goog.provide('goog.a');" +
         "goog.provide('goog.a.b');" +
         "if (x) {" +
         "  goog.a.b = 1;" +
         "} else {" +
         "  goog.a.b = 2;" +
         "}",

         "goog.a={};" +
         "if(x)" +
         "  goog.a.b=1;" +
         "else" +
         "  goog.a.b=2;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4b
  public void testProvideOrder4b() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    test("goog.provide('goog.a');" +
         "goog.provide('goog.a.b');" +
         "if (x) {" +
         "  goog.a.b = 1;" +
         "} else {" +
         "  goog.a.b = 2;" +
         "}",

         "goog.a={};" +
         "if(x)" +
         "  goog.a.b=1;" +
         "else" +
         "  goog.a.b=2;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidProvide
  public void testInvalidProvide() {
    test("goog.provide('a.class');", null, INVALID_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase1
  public void testInvalidBase1() {
    test("goog.base(this, 'method');", null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase2
  public void testInvalidBase2() {
    test("function Foo() {}" +
         "Foo.method = function() {" +
         "  goog.base(this, 'method');" +
         "};", null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase3
  public void testInvalidBase3() {
    test(String.format(METHOD_FORMAT, "goog.base();"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase4
  public void testInvalidBase4() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'bar');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase5
  public void testInvalidBase5() {
    test(String.format(METHOD_FORMAT, "goog.base('foo', 'method');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase6
  public void testInvalidBase6() {
    test(String.format(METHOD_FORMAT, "goog.base.call(null, this, 'method');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase7
  public void testInvalidBase7() {
    test("function Foo() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase8
  public void testInvalidBase8() {
    test("var Foo = function() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase9
  public void testInvalidBase9() {
    test("var goog = {}; goog.Foo = function() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase1
  public void testValidBase1() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'method');"),
         String.format(METHOD_FORMAT, "Foo.superClass_.method.call(this)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase2
  public void testValidBase2() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'method', 1, 2);"),
         String.format(METHOD_FORMAT,
             "Foo.superClass_.method.call(this, 1, 2)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase3
  public void testValidBase3() {
    test(String.format(METHOD_FORMAT, "return goog.base(this, 'method');"),
         String.format(METHOD_FORMAT,
             "return Foo.superClass_.method.call(this)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase4
  public void testValidBase4() {
    test("function Foo() { goog.base(this, 1, 2); }" + FOO_INHERITS,
         "function Foo() { BaseFoo.call(this, 1, 2); } " + FOO_INHERITS);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase5
  public void testValidBase5() {
    test("var Foo = function() { goog.base(this, 1); };" + FOO_INHERITS,
         "var Foo = function() { BaseFoo.call(this, 1); }; " + FOO_INHERITS);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase6
  public void testValidBase6() {
    test("var goog = {}; goog.Foo = function() { goog.base(this); }; " +
         "goog.inherits(goog.Foo, goog.BaseFoo);",
         "var goog = {}; goog.Foo = function() { goog.BaseFoo.call(this); }; " +
         "goog.inherits(goog.Foo, goog.BaseFoo);");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitAndExplicitProvide
  public void testImplicitAndExplicitProvide() {
    test("var goog = {}; " +
         "goog.provide('goog.foo.bar'); goog.provide('goog.foo');",
         "var goog = {}; goog.foo = {}; goog.foo.bar = {};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules
  public void testImplicitProvideInIndependentModules() {
    test(
        createModuleStar(
            "",
            "goog.provide('apps.A');",
            "goog.provide('apps.B');"),
        new String[] {
            "var apps = {};",
            "apps.A = {};",
            "apps.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules2
  public void testImplicitProvideInIndependentModules2() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.A');",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "apps.foo.A = {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules3
  public void testImplicitProvideInIndependentModules3() {
    test(
        createModuleStar(
            "var goog = {};",
            "goog.provide('goog.foo.A');",
            "goog.provide('goog.foo.B');"),
        new String[] {
            "var goog = {}; goog.foo = {};",
            "goog.foo.A = {};",
            "goog.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules1
  public void testProvideInIndependentModules1() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo');",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules2
  public void testProvideInIndependentModules2() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo'); apps.foo = {};",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {};",
            "apps.foo = {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireOfBaseGoog
  public void testRequireOfBaseGoog() {
    test("goog.require('goog');",
         "", MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine1
  public void testBasicDefine1() {
    test(" var DEF = true", "var DEF=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine2
  public void testBasicDefine2() {
    test(" var DEF = 'a'", "var DEF=\"a\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine3
  public void testBasicDefine3() {
    test(" var DEF = 0", "var DEF=0");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineBadType
  public void testDefineBadType() {
    test(" var DEF = {}",
        null, ProcessDefines.INVALID_DEFINE_TYPE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithBadValue
  public void testDefineWithBadValue() {
    test(" var DEF = new Boolean(true);", null,
        ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithDependentValue
  public void testDefineWithDependentValue() {
    test(" var BASE = false;\n" +
         " var DEF = !BASE;",
         "var BASE=false;var DEF=!BASE");
    test("var a = {};\n" +
         " a.BASE = false;\n" +
         " a.DEF = !a.BASE;",
         "var a={};a.BASE=false;a.DEF=!a.BASE");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithInvalidDependentValue
  public void testDefineWithInvalidDependentValue() {
    test("var BASE = false;\n" +
         " var DEF = !BASE;",
         null,
          ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding1
  public void testOverriding1() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    overrides.put("DEF_OVERRIDE_TO_FALSE", new Node(Token.FALSE));
    test(
        " var DEF_OVERRIDE_TO_TRUE = false;" +
        " var DEF_OVERRIDE_TO_FALSE = true",
        "var DEF_OVERRIDE_TO_TRUE=true;var DEF_OVERRIDE_TO_FALSE=false");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding2
  public void testOverriding2() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    String normalConst = "var DEF_OVERRIDE_TO_FALSE=true;";
    testWithPrefix(
        normalConst,
        " var DEF_OVERRIDE_TO_TRUE = false",
        "var DEF_OVERRIDE_TO_TRUE=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding3
  public void testOverriding3() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    test(
        " var DEF_OVERRIDE_TO_TRUE = true;",
        "var DEF_OVERRIDE_TO_TRUE=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testMisspelledOverride
  public void testMisspelledOverride() {
    overrides.put("DEF_BAD_OVERIDE", new Node(Token.TRUE));
    test(" var DEF_BAD_OVERRIDE = true",
        "var DEF_BAD_OVERRIDE=true", null,
        ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testCompiledIsKnownDefine
  public void testCompiledIsKnownDefine() {
    overrides.put("COMPILED", new Node(Token.TRUE));
    testSame("");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign1
  public void testSimpleReassign1() {
    test(" var DEF = false; DEF = true;",
        "var DEF=true;true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign2
  public void testSimpleReassign2() {
    test(" var DEF=false;DEF=true;DEF=3",
        "var DEF=3;true;3");

    Name def = namespace.getNameIndex().get("DEF");
    assertEquals(0, def.refs.size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign3
  public void testSimpleReassign3() {
    test(" var DEF = false;var x;x = DEF = true;",
        "var DEF=true;var x;x=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDuplicateVar
  public void testDuplicateVar() {
    test(" var DEF = false; var DEF = true;",
         null, VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignBeforeDeclaration1
  public void testAssignBeforeDeclaration1() {
    test("DEF=false;var b=false,DEF=true,c=false",
         null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignBeforeDeclaration2
  public void testAssignBeforeDeclaration2() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    test(
        "DEF_OVERRIDE_TO_TRUE = 3;" +
        " var DEF_OVERRIDE_TO_TRUE = false;",
        null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testEmptyDeclaration
  public void testEmptyDeclaration() {
    test(" var DEF;",
         null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterCall
  public void testReassignAfterCall() {
    test("var DEF=true;externMethod();DEF=false",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterRef
  public void testReassignAfterRef() {
    test("var DEF=true;var x = DEF;DEF=false",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignWithExpr
  public void testReassignWithExpr() {
    test("var DEF=true;var x;DEF=x=false",
        null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterNonGlobalRef
  public void testReassignAfterNonGlobalRef() {
    test(
        "var DEF=true;" +
        "var x=function(){var y=DEF}; DEF=false",
        "var DEF=false;var x=function(){var y=DEF};false");

    Name def = namespace.getNameIndex().get("DEF");
    assertEquals(1, def.refs.size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterRefInConditional
  public void testReassignAfterRefInConditional() {
    test(
        "var DEF=true;" +
        "if (false) {var x=DEF} DEF=false;",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignInNonGlobalScope
  public void testAssignInNonGlobalScope() {
    test("var DEF=true;function foo() {DEF=false};",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDeclareInNonGlobalScope
  public void testDeclareInNonGlobalScope() {
    test("function foo() {var DEF=true;};",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }
