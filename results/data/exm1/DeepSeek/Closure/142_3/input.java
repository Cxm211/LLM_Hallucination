// buggy code
  public void enterScope(NodeTraversal t) {
    // TODO(user): We CAN do this in the global scope, just need to be
    // careful when something is exported. Liveness uses bit-vector for live
    // sets so I don't see compilation time will be a problem for running this
    // pass in the global scope.
    Scope scope = t.getScope();
    if (scope.isGlobal()) {
      return;
    }
    ControlFlowGraph<Node> cfg = t.getControlFlowGraph();

    LiveVariablesAnalysis liveness =
        new LiveVariablesAnalysis(cfg, scope, compiler);
    // If the function has exactly 2 params, mark them as escaped. This is
    // a work-around for an IE bug where it throws an exception if you
    // write to the parameters of the callback in a sort(). See:
    // http://code.google.com/p/closure-compiler/issues/detail?id=58
    liveness.analyze();

    UndiGraph<Var, Void> interferenceGraph =
        computeVariableNamesInterferenceGraph(
            t, cfg, liveness.getEscapedLocals());

    GraphColoring<Var, Void> coloring =
        new GreedyGraphColoring<Var, Void>(interferenceGraph,
            coloringTieBreaker);

    coloring.color();
    colorings.push(coloring);
  }

  private ExtractionInfo extractMultilineTextualBlock(JsDocToken token,
                                                      WhitespaceOption option) {

    if (token == JsDocToken.EOC || token == JsDocToken.EOL ||
        token == JsDocToken.EOF) {
      return new ExtractionInfo("", token);
    }

    stream.update();
    int startLineno = stream.getLineno();
    int startCharno = stream.getCharno() + 1;

    // Read the content from the first line.
    String line = stream.getRemainingJSDocLine();

    if (option != WhitespaceOption.PRESERVE) {
      line = line.trim();
    }

    StringBuilder builder = new StringBuilder();
    builder.append(line);

    state = State.SEARCHING_ANNOTATION;
    token = next();

    boolean ignoreStar = false;

    do {
      switch (token) {
        case STAR:
          if (!ignoreStar) {
            if (builder.length() > 0) {
              builder.append(' ');
            }

            builder.append('*');
          }

          token = next();
          continue;

        case EOL:
          if (option != WhitespaceOption.SINGLE_LINE) {
            builder.append("\n");
          }

          ignoreStar = true;
          token = next();
          continue;

        case ANNOTATION:
        case EOC:
        case EOF:
          // When we're capturing a license block, annotations
          // in the block are ok.
            String multilineText = builder.toString();

            if (option != WhitespaceOption.PRESERVE) {
              multilineText = multilineText.trim();
            }

            int endLineno = stream.getLineno();
            int endCharno = stream.getCharno();

            if (multilineText.length() > 0) {
              jsdocBuilder.markText(multilineText, startLineno, startCharno,
                  endLineno, endCharno);
            }

            return new ExtractionInfo(multilineText, token);

          // FALL THROUGH

        default:
          ignoreStar = false;
          state = State.SEARCHING_ANNOTATION;

          if (builder.length() > 0) {
            builder.append(' ');
          }

          builder.append(toString(token));

          line = stream.getRemainingJSDocLine();

          if (option != WhitespaceOption.PRESERVE) {
            line = trimEnd(line);
          }

          builder.append(line);
          token = next();
      }
    } while (true);
  }

// relevant test
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

// com.google.javascript.jscomp.NodeTypeNormalizerTest::testJsDocNormalization
  public void testJsDocNormalization() throws Exception {
    Node root = parseExpectedJs(
        "var x = { a: function() {}," +
        "         c:  ('d')};");
    Node objlit = root.getFirstChild().getFirstChild().getFirstChild()
        .getFirstChild();
    assertEquals(Token.OBJECTLIT, objlit.getType());

    Node firstKey = objlit.getFirstChild();
    Node firstVal = firstKey.getNext();

    Node secondKey = firstVal.getNext();
    Node secondVal = secondKey.getNext();
    assertNotNull(firstKey.getJSDocInfo());
    assertNotNull(firstVal.getJSDocInfo());
    assertNull(secondKey.getJSDocInfo());
    assertNotNull(secondVal.getJSDocInfo());
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

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstantProperties
  public void testRenamingConstantProperties() {
    
    
    
    new WithCollapse().testConstantProperties();
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

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine
  public void testBasicDefine() {
    test(" var DEF = true", "var DEF=true");
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

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineAssignmentInLoop
  public void testDefineAssignmentInLoop() {
    test("var DEF=true;var x=0;while (x) {DEF=false;}",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testWithNoDefines
  public void testWithNoDefines() {
    testSame("var DEF=true;var x={};x.foo={}");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine1
  public void testNamespacedDefine1() {
    test("var a = {};  a.B = false; a.B = true;",
         "var a = {}; a.B = true; true;");

    Name aDotB = namespace.getNameIndex().get("a.B");
    assertEquals(0, aDotB.refs.size());
    assertEquals(1, aDotB.globalSets);
    assertNotNull(aDotB.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2
  public void testNamespacedDefine2() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};  a.B = false;",
         "var a = {}; a.B = true;");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine3
  public void testNamespacedDefine3() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};", "var a = {};", null,
         ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverrideAfterAlias
  public void testOverrideAfterAlias() {
    test("var x; var DEF=true; x=DEF; DEF=false;",
         null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns1
  public void testAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externSef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns2
  public void testAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externSef2()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns3
  public void testAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externNsef1()", ImmutableList.of("externNsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns4
  public void testAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externNsef2()", ImmutableList.of("externNsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns5
  public void testAnnotationInExterns5() throws Exception {
    checkMarkedCalls("externNsef3()", ImmutableList.of("externNsef3"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns1
  public void testNamespaceAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externObj.sef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns2
  public void testNamespaceAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externObj.nsef1()", ImmutableList.of("externObj.nsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns3
  public void testNamespaceAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externObj.nsef2()", ImmutableList.of("externObj.nsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns4
  public void testNamespaceAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externObj.partialFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns5
  public void testNamespaceAnnotationInExterns5() throws Exception {
    
    
    
    String templateSrc = "var o = {}; o.<fnName> = function(){}; o.<fnName>()";

    
    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "notPartialFn"),
                     ImmutableList.of("o.notPartialFn"));

    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "partialFn"),
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns6
  public void testNamespaceAnnotationInExterns6() throws Exception {
    checkMarkedCalls("externObj.partialSharedFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns1
  public void testConstructorAnnotationInExterns1() throws Exception {
    checkMarkedCalls("new externSefConstructor()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns2
  public void testConstructorAnnotationInExterns2() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.sefFnOfSefObj()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns3
  public void testConstructorAnnotationInExterns3() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.nsefFnOfSefObj()",
                     ImmutableList.of("a.nsefFnOfSefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns4
  public void testConstructorAnnotationInExterns4() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.externShared()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns5
  public void testConstructorAnnotationInExterns5() throws Exception {
    checkMarkedCalls("new externNsefConstructor()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns6
  public void testConstructorAnnotationInExterns6() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.sefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns7
  public void testConstructorAnnotationInExterns7() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.nsefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor",
                                      "a.nsefFnOfNsefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns8
  public void testConstructorAnnotationInExterns8() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName1
  public void testSharedFunctionName1() throws Exception {
    checkMarkedCalls("var a; " +
                     "if (true) {" +
                     "  a = new externNsefConstructor()" +
                     "} else {" +
                     "  a = new externSefConstructor()" +
                     "}" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName2
  public void testSharedFunctionName2() throws Exception {
    
    
    boolean broken = true;
    if (broken) {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2"));
    } else {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2",
                                        "a.externShared"));
    }
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNoSideEffectsSimple
  public void testNoSideEffectsSimple() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");

    checkMarkedCalls(
        prefix + "" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; return a" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "return externObj" + suffix, expected);
    checkMarkedCalls(
        "function g(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, expected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testExternCalls
  public void testExternCalls() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";

    checkMarkedCalls(prefix + "externNsef1()" + suffix,
                     ImmutableList.of("externNsef1", "f"));
    checkMarkedCalls(prefix + "externObj.nsef1()" + suffix,
                     ImmutableList.of("externObj.nsef1", "f"));

    checkMarkedCalls(prefix + "externSef1()" + suffix,
                     ImmutableList.<String>of());
    checkMarkedCalls(prefix + "externObj.sef1()" + suffix,
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testApply
  public void testApply() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.apply()",
                     ImmutableList.of("f.apply"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCall
  public void testCall() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.call()",
                     ImmutableList.<String>of("f.call"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference1
  public void testInference1() throws Exception {
    checkMarkedCalls("function f() {return g()}" +
                     "function g() {return 42}" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference2
  public void testInference2() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "function f() {g()}" +
                     "function g() {a=2}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference3
  public void testInference3() throws Exception {
    checkMarkedCalls("var f = function() {return g()};" +
                     "var g = function() {return 42};" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference4
  public void testInference4() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var f = function() {g()};" +
                     "var g = function() {a=2};" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference5
  public void testInference5() throws Exception {
    checkMarkedCalls("var goog = {};" +
                     "goog.f = function() {return goog.g()};" +
                     "goog.g = function() {return 42};" +
                     "goog.f()",
                     ImmutableList.of("goog.g", "goog.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference6
  public void testInference6() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var goog = {};" +
                     "goog.f = function() {goog.g()};" +
                     "goog.g = function() {a=2};" +
                     "goog.f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators1
  public void testUnaryOperators1() throws Exception {
    checkMarkedCalls("function f() {var x = 1; x++}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators2
  public void testUnaryOperators2() throws Exception {
    checkMarkedCalls("var x = 1;" +
                     "function f() {x++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators3
  public void testUnaryOperators3() throws Exception {
    checkMarkedCalls("function f() {var x = {foo : 0}; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators4
  public void testUnaryOperators4() throws Exception {
    checkMarkedCalls("var x = {foo : 0};" +
                     "function f() {x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators5
  public void testUnaryOperators5() throws Exception {
    checkMarkedCalls("function f(x) {x.foo++}" +
                     "f({foo : 0})",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator1
  public void testDeleteOperator1() throws Exception {
    checkMarkedCalls("var x = {};" +
                     "function f() {delete x}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator2
  public void testDeleteOperator2() throws Exception {
    checkMarkedCalls("function f() {var x = {}; delete x}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator1
  public void testOrOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator2
  public void testOrOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator3
  public void testOrOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperators4
  public void testOrOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator1
  public void testAndOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator2
  public void testAndOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator3
  public void testAndOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperators4
  public void testAndOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator1
  public void testHookOperator1() throws Exception {
    checkMarkedCalls("var f = true ? externNsef1 : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator2
  public void testHookOperator2() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator3
  public void testHookOperator3() throws Exception {
    checkMarkedCalls("var f = true ? externNsef2 : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperators4
  public void testHookOperators4() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow1
  public void testThrow1() throws Exception {
    checkMarkedCalls("function f(){throw Error()};\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow2
  public void testThrow2() throws Exception {
    checkMarkedCalls("function A(){throw Error()};\n" +
                     "function f(){return new A()}\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAssignmentOverride
  public void testAssignmentOverride() throws Exception {
    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var a = new A;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A", "a.foo"));

    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var x = 1\n" +
                     "function f(){x = 10}\n" +
                     "var a = new A;\n" +
                     "a.foo = f;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance1
  public void testInheritance1() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){var data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source,
                     ImmutableList.of("this.foo", "goog.inherits",
                                      "I", "i.foo", "i.bar",
                                      "A", "a.foo", "a.bar"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance2
  public void testInheritance2() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source, ImmutableList.of("goog.inherits", "I", "A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallBeforeDefinition
  public void testCallBeforeDefinition() throws Exception {
    checkMarkedCalls("f(); function f(){}",
                     ImmutableList.of("f"));

    checkMarkedCalls("var a = {}; a.f(); a.f = function (){}",
                     ImmutableList.of("a.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis1
  public void testConstructorThatModifiesThis1() throws Exception {
    String source = "function A(){this.foo = 1}\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis2
  public void testConstructorThatModifiesThis2() throws Exception {
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis3
  public void testConstructorThatModifiesThis3() throws Exception {

    
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.bar()};\n" +
        "A.prototype.bar = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis4
  public void testConstructorThatModifiesThis4() throws Exception {

    
    String source = "function A(){foo.call(this)}\n" +
        "function foo(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal1
  public void testConstructorThatModifiesGlobal1() throws Exception {
    String source = "var b = 0;" +
        "function A(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal2
  public void testConstructorThatModifiesGlobal2() throws Exception {
    String source = "var b = 0;" +
        "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionThatModifiesThis
  public void testCallFunctionThatModifiesThis() throws Exception {
    String source = "function A(){}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f(){var a = new A; return a}\n" +
        "function g(){var a = new A; a.foo(); return a}\n" +
        "f(); g()";

    checkMarkedCalls(source, ImmutableList.<String>of("A", "A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrG
  public void testCallFunctionFOrG() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f || g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHook
  public void testCallFunctionFOrGViaHook() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionForGorH
  public void testCallFunctionForGorH() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){}\n" +
        "function i(){ (false ? f : (g || h))() }\n" +
        "i()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : (g || h))", "i"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGWithSideEffects
  public void testCallFunctionFOrGWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "function i(){ (g || f)() }\n" +
        "function j(){ (f || f)() }\n" +
        "function k(){ (g || g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g || g)", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHookWithSideEffects
  public void testCallFunctionFOrGViaHookWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "function i(){ (false ? g : f)() }\n" +
        "function j(){ (false ? f : f)() }\n" +
        "function k(){ (false ? g : g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g : g)", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation1
  public void testInvalidAnnotation1() throws Exception {
    test(" function foo() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation2
  public void testInvalidAnnotation2() throws Exception {
    test("var f =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation3
  public void testInvalidAnnotation3() throws Exception {
    test(" var f = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation4
  public void testInvalidAnnotation4() throws Exception {
    test("var f = function() {};" +
         " f.x = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation5
  public void testInvalidAnnotation5() throws Exception {
    test("var f = function() {};" +
         "f.x =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryCatch
  public void testRemoveTryCatch() {
    test("try{var a=1;}catch(ex){var b=2;}",
         "var b;var a=1");
    test("try{var a=1;var b=2}catch(ex){var c=3;var d=4;}",
         "var d;var c;{var a=1;var b=2}");
    test("try{var a=1;var b=2}catch(ex){}",
         "{var a=1;var b=2}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryFinally
  public void testRemoveTryFinally() {
    test("try{var a=1;}finally{var c=3;}",
         "var a=1;var c=3");
    test("try{var a=1;var b=2}finally{var e=5;var f=6;}",
         "{var a=1;var b=2}{var e=5;var f=6}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryCatchFinally
  public void testRemoveTryCatchFinally() {
    test("try{var a=1;}catch(ex){var b=2;}finally{var c=3;}",
         "var b;var a=1;var c=3");
    test("try{var a=1;var b=2}catch(ex){var c=3;var d=4;}finally{var e=5;" +
         "var f=6;}",
         "var d;var c;{var a=1;var b=2}{var e=5;var f=6}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testPreserveTryBlockContainingReturnStatement
  public void testPreserveTryBlockContainingReturnStatement() {
    testSame("function(){var a;try{a=1;return}finally{a=2}}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testPreserveAnnotatedTryBlock
  public void testPreserveAnnotatedTryBlock() {
    test("try{var a=1;}catch(ex){var b=2;}",
         "try{var a=1}catch(ex){var b=2}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testIfTryFinally
  public void testIfTryFinally() {
    test("if(x)try{y}finally{z}", "if(x){y;z}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testIfTryCatch
  public void testIfTryCatch() {
    test("if(x)try{y;z}catch(e){}", "if(x){y;z}");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties
  public void testAnalyzeUnusedPrototypeProperties() {
    
    test(" \n" +
        "function e(){} \n" +
        "e.prototype.a = function(){};" +
        "e.prototype.b = function(){};" +
        "var x = new e; x.a()",

        "function e(){}" +
        " e.prototype.a = function(){};" +
        "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties2
  public void testAnalyzeUnusedPrototypeProperties2() {
    
    
    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties3
  public void testAnalyzeUnusedPrototypeProperties3() {
    
    
    test(" \n" +
        "function e(){} \n" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           
           "var x = new e; x.a()");

    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAliasing
  public void testAliasing() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testStatement
  public void testStatement() {
    test(" \n" +
        "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameSimple
  public void testRenameSimple() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameGlobals
  public void testRenameGlobals() {
    test("var Foo; var Bar, y; function x() { Bar++; }",
         "var a; var b, c; function d() { b++; }");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameLocals
  public void testRenameLocals() {
    test("(function (v1, v2) {}); (function (v3, v4) {});",
        "(function (a, b) {}); (function (a, b) {});");
    test("function f1(v1, v2) {}; function f2(v3, v4) {};",
        "function c(a, b) {}; function d(a, b) {};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameRedeclaredGlobals
  public void testRenameRedeclaredGlobals() {
    test("function f1(v1, v2) {f1()};" +
         "" +
         "function f1(v3, v4) {f1()};",
         "function a(b, c) {a()};" +
         "function a(b, c) {a()};");

    localRenamingOnly = true;

    test("function f1(v1, v2) {f1()};" +
        "" +
        "function f1(v3, v4) {f1()};",
        "function f1(a, b) {f1()};" +
        "function f1(a, b) {f1()};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRecursiveFunctions1
  public void testRecursiveFunctions1() {
    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var d = function a(b, c) {" +
         "  a(b, c);" +
         "};");

    localRenamingOnly = true;

    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var walk = function a(b, c) {" +
         "  a(b, c);" +
         "};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRecursiveFunctions2
  public void testRecursiveFunctions2() {
    preserveAnonymousFunctionNames = true;

    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var c = function walk(a, b) {" +
         "  walk(a, b);" +
         "};");

    localRenamingOnly = true;

    test("var walk = function walk(node, aFunction) {" +
        "  walk(node, aFunction);" +
        "};",
        "var walk = function walk(a, b) {" +
        "  walk(a, b);" +
        "};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameLocalsClashingWithGlobals
  public void testRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
        "function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameNested
  public void testRenameNested() {
    test("function f1(v1, v2) { (function(v3, v4) {}) }",
         "function a(b, c) { (function(d, e) {}) }");
    test("function f1(v1, v2) { function f2(v3, v4) {} }",
         "function a(b, c) { function d(e, f) {} }");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithExterns1
  public void testRenameWithExterns1() {
    String externs = "var foo;";
    test(externs, "var bar; foo(bar);", "var a; foo(a);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithExterns2
  public void testRenameWithExterns2() {
    String externs = "var a;";
    test(externs, "var b = 5", "var b = 5", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testDoNotRenameExportedName
  public void testDoNotRenameExportedName() {
    test("_foo()", "_foo()");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithNameOverlap
  public void testRenameWithNameOverlap() {
    test("var a = 1; var b = 2; b + b;",
         "var a = 1; var b = 2; b + b;");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix1
  public void testRenameWithPrefix1() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
        "function PRE_(a, b) {return a} PRE_();");
    prefix = DEFAULT_PREFIX;

  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix2
  public void testRenameWithPrefix2() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {var v3 = v1 + v2; return v3;} Foo();",
        "function PRE_(a, b) {var c = a + b; return c;} PRE_();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix3
  public void testRenameWithPrefix3() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

        "function a() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameVarsTest::testNamingBasedOnOrderOfOccurrence
  public void testNamingBasedOnOrderOfOccurrence() {
    test("var q,p,m,n,l,k; " +
             "(function (r) {}); try { } catch(s) {}; var t = q + q;",
         "var a,b,c,d,e,f; " +
             "(function(g) {}); try { } catch(h) {}; var i = a + a;"
         );
    test("function(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z," +
         "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,$){};" +
         "var a4,a3,a2,a1,b4,b3,b2,b1,ab,ac,ad,fg;function foo(){};",
         "function(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$){};" +
         "var aa,ba,ca,da,ea,fa,ga,ha,ia,ja,ka,la;function ma(){};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimple
  public void testStableRenameSimple() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c");
    testRenameMap("function Foo(v1, v2) {return v1;} Foo();",
                  "function a(b, c) {return b;} a();", expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c", "L 2", "d");
    testRenameMapUsingOldMap("function Foo(v1, v2, v3) {return v1;} Foo();",
         "function a(b, c, d) {return b;} a();", expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameGlobals
  public void testStableRenameGlobals() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "Bar", "b", "y", "c", "x", "d");
    testRenameMap("var Foo; var Bar, y; function x() { Bar++; }",
                  "var a; var b, c; function d() { b++; }",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "Foo", "a", "Bar", "b", "y", "c", "x", "d", "Baz", "f", "L 0" , "e");
    testRenameMapUsingOldMap(
        "var Foo, Baz; var Bar, y; function x(R) { return R + Bar++; }",
        "var a, f; var b, c; function d(e) { return e + b++; }",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPointlesslyAnonymousFunctions
  public void testStableRenameWithPointlesslyAnonymousFunctions() {
    VariableMap expectedVariableMap = makeVariableMap("L 0", "a", "L 1", "b");
    testRenameMap("function (v1, v2) {}; function (v3, v4) {};",
                  "function (a, b) {}; function (a, b) {};",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap("L 0", "a", "L 1", "b", "L 2", "c");
    testRenameMapUsingOldMap("function (v0, v1, v2) {}; function (v3, v4) {};",
                             "function (a, b, c) {}; function (a, b) {};",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameLocalsClashingWithGlobals
  public void testStableRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
         "function a(b, c) {return b;} a();");
    previouslyUsedMap = renameVars.getVariableMap();
    test("function bar(){return;}function a(v1, v2) {return v1;} a();",
         "function d(){return;}function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameNested
  public void testStableRenameNested() {
    VariableMap expectedVariableMap = makeVariableMap(
        "f1", "a", "L 0", "b", "L 1", "c", "L 2", "d", "L 3", "e");
    testRenameMap("function f1(v1, v2) { (function(v3, v4) {}) }",
                  "function a(b, c) { (function(d, e) {}) }",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "f1", "a", "L 0", "b", "L 1", "c", "L 2", "d", "L 3", "e", "L 4", "f");
    testRenameMapUsingOldMap("function f1(v1, v2) { (function(v3, v4, v5) {}) }",
                             "function a(b, c) { (function(d, e, f) {}) }",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithExterns1
  public void testStableRenameWithExterns1() {
    String externs = "var foo;";
    test(externs, "var bar; foo(bar);", "var a; foo(a);", null, null);
    previouslyUsedMap = renameVars.getVariableMap();
    test(externs, "var bar, baz; foo(bar, baz);",
         "var a, b; foo(a, b);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithExterns2
  public void testStableRenameWithExterns2() {
    String externs = "var a;";
    test(externs, "var b = 5", "var b = 5", null, null);
    previouslyUsedMap = renameVars.getVariableMap();
    test(externs, "var b = 5, catty = 9;", "var b = 5, c=9;", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithNameOverlap
  public void testStableRenameWithNameOverlap() {
    test("var a = 1; var b = 2; b + b;",
         "var a = 1; var b = 2; b + b;");
    previouslyUsedMap = renameVars.getVariableMap();
    test("var a = 1; var c, b = 2; b + b;",
         "var a = 1; var c, b = 2; b + b;");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithAnonymousFunctions
  public void testStableRenameWithAnonymousFunctions() {
    VariableMap expectedVariableMap = makeVariableMap("L 0", "a", "foo", "b");
    testRenameMap("function foo(bar){return bar;}foo(function(h){return h;});",
                  "function b(a){return a}b(function(a){return a;})",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap("foo", "b", "L 0", "a", "L 1", "c");
    testRenameMapUsingOldMap(
        "function foo(bar) {return bar;}foo(function(g,h) {return g+h;});",
        "function b(a){return a}b(function(a,c){return a+c;})",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleExternsChanges
  public void testStableRenameSimpleExternsChanges() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c");
    testRenameMap("function Foo(v1, v2) {return v1;} Foo();",
                  "function a(b, c) {return b;} a();", expectedVariableMap);

    expectedVariableMap = makeVariableMap("L 0", "b", "L 1", "c", "L 2", "a");
    String externs = "var Foo;";
    testRenameMapUsingOldMap(externs,
                             "function Foo(v1, v2, v0) {return v1;} Foo();",
                             "function Foo(b, c, a) {return b;} Foo();",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleLocalNameExterned
  public void testStableRenameSimpleLocalNameExterned() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");

    previouslyUsedMap = renameVars.getVariableMap();

    String externs = "var b;";
    test(externs, "function Foo(v1, v2) {return v1;} Foo(b);",
         "function a(d, c) {return d;} a(b);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleGlobalNameExterned
  public void testStableRenameSimpleGlobalNameExterned() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");

    previouslyUsedMap = renameVars.getVariableMap();

    String externs = "var Foo;";
    test(externs, "function Foo(v1, v2, v0) {return v1;} Foo();",
         "function Foo(b, c, a) {return b;} Foo();", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPrefix1AndUnstableLocalNames
  public void testStableRenameWithPrefix1AndUnstableLocalNames() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
         "function PRE_(a, b) {return a} PRE_();");

    previouslyUsedMap = renameVars.getVariableMap();

    prefix = "PRE_";
    test("function Foo(v0, v1, v2) {return v1} Foo();",
         "function PRE_(a, b, c) {return b} PRE_();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPrefix2
  public void testStableRenameWithPrefix2() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function a() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");

    previouslyUsedMap = renameVars.getVariableMap();

    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Baz() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function a() {return 1;}" +
         "function ab() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testContrivedExampleWhereConsistentRenamingIsWorse
  public void testContrivedExampleWhereConsistentRenamingIsWorse() {
    previouslyUsedMap = makeVariableMap(
        "Foo", "LongString", "L 0", "b", "L 1", "c");

    test("function Foo(v1, v2) {return v1;} Foo();",
         "function LongString(b, c) {return b;} LongString();");

    previouslyUsedMap = renameVars.getVariableMap();
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "LongString", "L 0", "b", "L 1", "c");
    assertVariableMapsEqual(expectedVariableMap, previouslyUsedMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportSimpleSymbolReservesName
  public void testExportSimpleSymbolReservesName() {
    test("var goog, x; goog.exportSymbol('a', x);",
         "var a, b; a.exportSymbol('a', b);");
    withClosurePass = true;
    test("var goog, x; goog.exportSymbol('a', x);",
         "var b, c; b.exportSymbol('a', c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportComplexSymbolReservesName
  public void testExportComplexSymbolReservesName() {
    test("var goog, x; goog.exportSymbol('a.b', x);",
         "var a, b; a.exportSymbol('a.b', b);");
    withClosurePass = true;
    test("var goog, x; goog.exportSymbol('a.b', x);",
         "var b, c; b.exportSymbol('a.b', c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportToNonStringDoesntExplode
  public void testExportToNonStringDoesntExplode() {
    withClosurePass = true;
    test("var goog, a, b; goog.exportSymbol(a, b);",
         "var a, b, c; a.exportSymbol(b, c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testDollarSignSuperExport1
  public void testDollarSignSuperExport1() {
    useGoogleCodingConvention = false;
    
    test("var x = function($super,duper,$fantastic){}",
         "var c = function($super,    a,        b){}");

    localRenamingOnly = false;
    test("var $super = 1", "var a = 1");

    useGoogleCodingConvention = true;
    test("var x = function($super,duper,$fantastic){}",
         "var d = function(a,     b,    c        ){}");
  }

// com.google.javascript.jscomp.RenameVarsTest::testDollarSignSuperExport2
  public void testDollarSignSuperExport2() {
    boolean normalizedExpectedJs = false;
    super.enableNormalize(false);

    useGoogleCodingConvention = false;
    
    test("var x = function($super,duper,$fantastic){};" +
            "var y = function($super,duper){};",
         "var c = function($super,    a,         b){};" +
            "var d = function($super,    a){};");

    localRenamingOnly = false;
    test("var $super = 1", "var a = 1");

    useGoogleCodingConvention = true;
    test("var x = function($super,duper,$fantastic){};" +
            "var y = function($super,duper){};",
         "var d = function(a,     b,    c         ){};" +
            "var e = function(     a,    b){};");

    super.disableNormalize();
  }

// com.google.javascript.jscomp.RenameVarsTest::testPseudoNames
  public void testPseudoNames() {
    generatePseudoNames = false;
    
    test("var foo = function(a, b, c){}",
         "var d = function(a, b, c){}");

    generatePseudoNames = true;
    test("var foo = function(a, b, c){}",
         "var $foo$$ = function($a$$, $b$$, $c$$){}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValue
  public void testValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNullValue
  public void testNullValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, [jscomp.typecheck.nullChecker]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValues
  public void testValues() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "  jscomp.typecheck.checkType(j, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testSkipParamOK
  public void testSkipParamOK() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  jscomp.typecheck.checkType(j, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUnion
  public void testUnion() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, [" +
        "      jscomp.typecheck.valueChecker('number'), " +
        "      jscomp.typecheck.valueChecker('string')" +
        "]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUntypedParam
  public void testUntypedParam() {
    testChecks(" function f(x) {}", "function f(x) {}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testReturn
  public void testReturn() {
    testChecks(" function f() { return 'x'; }",
        "function f() {" +
        "  return jscomp.typecheck.checkType('x', " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNativeClass
  public void testNativeClass() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, " +
        "      [jscomp.typecheck.externClassChecker('String')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testQualifiedClass
  public void testQualifiedClass() {
    testChecks("var goog = {}; goog.Foo = function() {};" +
        " function f(x) {}",
        "var goog = {}; goog.Foo = function() {};" +
        "goog.Foo.prototype['instance_of__goog.Foo'] = true;" +
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, " +
        "    [jscomp.typecheck.classChecker('goog.Foo')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInnerClasses
  public void testInnerClasses() {
    enableNormalize(false);
    testChecks(
        "function f() {  function inner() {} }" +
        "function g() {  function inner() {} }",
        "function f() {" +
        "   function inner() {}" +
        "  inner.prototype['instance_of__inner'] = true;" +
        "}" +
        "function g() {" +
        "   function inner$$1() {}" +
        "  inner$$1.prototype['instance_of__inner$$1'] = true;" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInterface
  public void testInterface() {
    testChecks("function I() {}" +
        "function f(i) {}",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "    [jscomp.typecheck.interfaceChecker('I')])" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterface
  public void testImplementedInterface() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testExtendedInterface
  public void testExtendedInterface() {
    testChecks("function I() {}" +
        "function J() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function J() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype['implements__J'] = true;");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterfaceOrdering
  public void testImplementedInterfaceOrdering() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}" +
        "C.prototype.f = function() {};",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterfaceOrderingGoogInherits
  public void testImplementedInterfaceOrderingGoogInherits() {
    testChecks("var goog = {}; goog.inherits = function(x, y) {};" +
        "function I() {}" +
        "function f(i) {}" +
        "function B() {}" +
        "function C() {}" +
        "goog.inherits(C, B);" +
        "C.prototype.f = function() {};",
        "var goog = {}; goog.inherits = function(x, y) {};" +
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function B() {}" +
        "B.prototype['instance_of__B'] = true;" +
        "function C() {}" +
        "goog.inherits(C, B);" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype.f = function() {};");
  }
