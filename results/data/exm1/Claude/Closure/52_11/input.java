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

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInnerConstructor
  public void testInnerConstructor() {
    testChecks("(function() {  function C() {} })()",
        "(function() {" +
        "  function C() {} C.prototype['instance_of__C'] = true;" +
        "})()");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testReturnNothing
  public void testReturnNothing() {
    testChecks("function f() { return; }", "function f() { return; }");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testFunctionType
  public void testFunctionType() {
    testChecks("function f() {}", "function f() {}");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testOneLevel
  public void testOneLevel() {
    testScoped("var g = goog;g.dom.createElement(g.dom.TagName.DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTwoLevel
  public void testTwoLevel() {
    testScoped("var d = goog.dom;d.createElement(d.TagName.DIV);",
               "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTransitive
  public void testTransitive() {
    testScoped("var d = goog.dom;var DIV = d.TagName.DIV;d.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTransitiveInSameVar
  public void testTransitiveInSameVar() {
    testScoped("var d = goog.dom, DIV = d.TagName.DIV;d.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testMultipleTransitive
  public void testMultipleTransitive() {
    testScoped(
        "var g=goog;var d=g.dom;var t=d.TagName;var DIV=t.DIV;" +
            "d.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testFourLevel
  public void testFourLevel() {
    testScoped("var DIV = goog.dom.TagName.DIV;goog.dom.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testWorksInClosures
  public void testWorksInClosures() {
    testScoped(
        "var DIV = goog.dom.TagName.DIV;" +
            "goog.x = function() {goog.dom.createElement(DIV);};",
        "goog.x = function() {goog.dom.createElement(goog.dom.TagName.DIV);};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testOverridden
  public void testOverridden() {
    
    
    testScopedNoChanges(
        "var g = goog;", "goog.x = function(g) {g.z()};");
    
    testScopedNoChanges(
        "var g = goog;", "goog.x = function() {var g = {}; g.z()};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTwoScopes
  public void testTwoScopes() {
    test(
        "goog.scope(function() {var g = goog;g.method()});" +
        "goog.scope(function() {g.method();});",
        "goog.method();g.method();");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTwoSymbolsInTwoScopes
  public void testTwoSymbolsInTwoScopes() {
    test(
        "var goog = {};" +
        "goog.scope(function() { var g = goog; g.Foo = function() {}; });" +
        "goog.scope(function() { " +
        "  var Foo = goog.Foo; goog.bar = function() { return new Foo(); };" +
        "});",
        "var goog = {};" +
        "goog.Foo = function() {};" +
        "goog.bar = function() { return new goog.Foo(); };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasOfSymbolInGoogScope
  public void testAliasOfSymbolInGoogScope() {
    test(
        "var goog = {};" +
        "goog.scope(function() {" +
        "  var g = goog;" +
        "  g.Foo = function() {};" +
        "  var Foo = g.Foo;" +
        "  Foo.prototype.bar = function() {};" +
        "});",
        "var goog = {}; goog.Foo = function() {};" +
        "goog.Foo.prototype.bar = function() {};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedFunctionReturnThis
  public void testScopedFunctionReturnThis() {
    test("goog.scope(function() { " +
         "  var g = goog; g.f = function() { return this; };" +
         "});",
         "goog.f = function() { return this; };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedFunctionAssignsToVar
  public void testScopedFunctionAssignsToVar() {
    test("goog.scope(function() { " +
         "  var g = goog; g.f = function(x) { x = 3; return x; };" +
         "});",
         "goog.f = function(x) { x = 3; return x; };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedFunctionThrows
  public void testScopedFunctionThrows() {
    test("goog.scope(function() { " +
         "  var g = goog; g.f = function() { throw 'error'; };" +
         "});",
         "goog.f = function() { throw 'error'; };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testPropertiesNotChanged
  public void testPropertiesNotChanged() {
    testScopedNoChanges("var x = goog.dom;", "y.x();");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testShadowedVar
  public void testShadowedVar() {
    test("var Popup = {};" +
         "var OtherPopup = {};" +
         "goog.scope(function() {" +
         "  var Popup = OtherPopup;" +
         "  Popup.newMethod = function() { return new Popup(); };" +
         "});",
         "var Popup = {};" +
         "var OtherPopup = {};" +
         "OtherPopup.newMethod = function() { return new OtherPopup(); };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocType
  public void testJsDocType() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocParameter
  public void testJsDocParameter() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocExtends
  public void testJsDocExtends() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocImplements
  public void testJsDocImplements() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocEnum
  public void testJsDocEnum() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocReturn
  public void testJsDocReturn() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocThis
  public void testJsDocThis() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocThrows
  public void testJsDocThrows() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocSubType
  public void testJsDocSubType() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocTypedef
  public void testJsDocTypedef() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testArrayJsDoc
  public void testArrayJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testObjectJsDoc
  public void testObjectJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testUnionJsDoc
  public void testUnionJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testFunctionJsDoc
  public void testFunctionJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testForwardJsDoc
  public void testForwardJsDoc() {
    testScoped(
        "\n" +
        "foo.Foo = function() {};" +
        " function actual(x) {3}" +
        "var Foo = foo.Foo;" +
        " Foo.Bar = function() {};" +
        " function expected(x) {}",

        "\n" +
        "foo.Foo = function() {};" +
        " function actual(x) {3}" +
        " foo.Foo.Bar = function() {};" +
        " function expected(x) {}");
    verifyTypes();
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTestTypes
  public void testTestTypes() {
    try {
      testTypes(
          "var x = goog.Timer;",
          ""
          + " types.actual;"
          + " types.expected;");
      fail("Test types should fail here.");
    } catch (AssertionError e) {
    }
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testNullType
  public void testNullType() {
    testTypes(
        "var x = goog.Timer;",
        " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedThis
  public void testScopedThis() {
    testScopedFailure("this.y = 10;", ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
    testScopedFailure("var x = this;",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
    testScopedFailure("fn(this);", ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasRedefinition
  public void testAliasRedefinition() {
    testScopedFailure("var x = goog.dom; x = goog.events;",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasNonRedefinition
  public void testAliasNonRedefinition() {
    test("var y = {}; goog.scope(function() { goog.dom = y; });",
         "var y = {}; goog.dom = y;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedReturn
  public void testScopedReturn() {
    testScopedFailure("return;", ScopedAliases.GOOG_SCOPE_USES_RETURN);
    testScopedFailure("var x = goog.dom; return;",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedThrow
  public void testScopedThrow() {
    testScopedFailure("throw 'error';", ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testUsedImproperly
  public void testUsedImproperly() {
    testFailure("var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testBadParameters
  public void testBadParameters() {
    testFailure("goog.scope()", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(10)", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(function() {}, 10)",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(function z() {})",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(function(a, b, c) {})",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testNonAliasLocal
  public void testNonAliasLocal() {
    testScopedFailure("var x = 10", ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
    testScopedFailure("var x = goog.dom + 10",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
    testScopedFailure("var x = goog['dom']",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
    testScopedFailure("var x = goog.dom, y = 10",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testNoGoogScope
  public void testNoGoogScope() {
    String fullJsCode =
        "var g = goog;\n g.dom.createElement(g.dom.TagName.DIV);";
    TransformationHandlerSpy spy = new TransformationHandlerSpy();
    transformationHandler = spy;
    test(fullJsCode, fullJsCode);

    assertTrue(spy.observedPositions.isEmpty());
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testRecordOneAlias
  public void testRecordOneAlias() {
    String fullJsCode = GOOG_SCOPE_START_BLOCK
        + "var g = goog;\n g.dom.createElement(g.dom.TagName.DIV);\n"
        + GOOG_SCOPE_END_BLOCK;
    String expectedJsCode = "goog.dom.createElement(goog.dom.TagName.DIV);\n";

    TransformationHandlerSpy spy = new TransformationHandlerSpy();
    transformationHandler = spy;
    test(fullJsCode, expectedJsCode);

    assertTrue(spy.observedPositions.containsKey("testcode"));
    List<SourcePosition<AliasTransformation>> positions =
        spy.observedPositions.get("testcode");
    assertEquals(1, positions.size());
    verifyAliasTransformationPosition(
        1, GOOG_SCOPE_LEN, 2, 1, positions.get(0));

    assertEquals(1, spy.constructedAliases.size());
    AliasSpy aliasSpy = (AliasSpy) spy.constructedAliases.get(0);
    assertEquals("goog", aliasSpy.observedDefinitions.get("g"));
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testRecordMultipleAliases
  public void testRecordMultipleAliases() {
    String fullJsCode = GOOG_SCOPE_START_BLOCK
        + "var g = goog;\n var b= g.bar;\n var f = goog.something.foo;"
        + "g.dom.createElement(g.dom.TagName.DIV);\n b.foo();"
        + GOOG_SCOPE_END_BLOCK;
    String expectedJsCode =
        "goog.dom.createElement(goog.dom.TagName.DIV);\n goog.bar.foo();";
    TransformationHandlerSpy spy = new TransformationHandlerSpy();
    transformationHandler = spy;
    test(fullJsCode, expectedJsCode);

    assertTrue(spy.observedPositions.containsKey("testcode"));
    List<SourcePosition<AliasTransformation>> positions =
        spy.observedPositions.get("testcode");
    assertEquals(1, positions.size());
    verifyAliasTransformationPosition(
        1, GOOG_SCOPE_LEN, 3, 1, positions.get(0));

    assertEquals(1, spy.constructedAliases.size());
    AliasSpy aliasSpy = (AliasSpy) spy.constructedAliases.get(0);
    assertEquals("goog", aliasSpy.observedDefinitions.get("g"));
    assertEquals("g.bar", aliasSpy.observedDefinitions.get("b"));
    assertEquals("goog.something.foo", aliasSpy.observedDefinitions.get("f"));
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testRecordAliasFromMultipleGoogScope
  public void testRecordAliasFromMultipleGoogScope() {
    String firstGoogScopeBlock = GOOG_SCOPE_START_BLOCK
        + "\n var g = goog;\n g.dom.createElement(g.dom.TagName.DIV);\n"
        + GOOG_SCOPE_END_BLOCK;
    String fullJsCode = firstGoogScopeBlock + "\n\nvar l = abc.def;\n\n"
        + GOOG_SCOPE_START_BLOCK
        + "\n var z = namespace.Zoo;\n z.getAnimals(l);\n"
        + GOOG_SCOPE_END_BLOCK;

    String expectedJsCode = "goog.dom.createElement(goog.dom.TagName.DIV);\n"
        + "\n\nvar l = abc.def;\n\n" + "\n namespace.Zoo.getAnimals(l);\n";

    TransformationHandlerSpy spy = new TransformationHandlerSpy();
    transformationHandler = spy;
    test(fullJsCode, expectedJsCode);

    assertTrue(spy.observedPositions.containsKey("testcode"));
    List<SourcePosition<AliasTransformation>> positions =
        spy.observedPositions.get("testcode");
    assertEquals(2, positions.size());

    verifyAliasTransformationPosition(
        1, GOOG_SCOPE_LEN, 6, 0, positions.get(0));

    verifyAliasTransformationPosition(
        8, GOOG_SCOPE_LEN, 11, 4, positions.get(1));

    assertEquals(2, spy.constructedAliases.size());
    AliasSpy aliasSpy = (AliasSpy) spy.constructedAliases.get(0);
    assertEquals("goog", aliasSpy.observedDefinitions.get("g"));

    aliasSpy = (AliasSpy) spy.constructedAliases.get(1);
    assertEquals("namespace.Zoo", aliasSpy.observedDefinitions.get("z"));
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowSimple1
  public void testShadowSimple1() {
    test("function foo(x) { return function (y) {} }",
         "function   b(a) { return function (a) {} }");

    generatePseudoNames = true;

    test("function  foo  ( x  ) { return function ( y  ) {} }",
         "function $foo$$($x$$) { return function ($x$$) {} }");

  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowSimple2
  public void testShadowSimple2() {
    test("function foo(x,y) { return function (y,z) {} }",
         "function   c(a,b) { return function (a,b) {} }");

    generatePseudoNames = true;

    test("function  foo  ( x  , y  ) { return function ( y  , z  ) {} }",
         "function $foo$$($x$$,$y$$) { return function ($x$$,$y$$) {} }");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowMostUsedVar
  public void testShadowMostUsedVar() {
    generatePseudoNames = true;
    test("function  foo  () {var  x  ; var  y  ;  y  ; y  ; y  ; x  ;" +
         "  return function ( k  ) {} }",

         "function $foo$$() {var $x$$; var $y$$; $y$$;$y$$;$y$$;$x$$;" +
         "  return function ($y$$) {} }");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testNoShadowReferencedVariables
  public void testNoShadowReferencedVariables() {
    generatePseudoNames = true;
    test("function  f1  () { var  x  ; x  ; x  ; x  ;" +
         "  return function  f2  ( y  ) {" +
         "    return function  f3  () { x  } }}",
         "function $f1$$() { var $x$$;$x$$;$x$$;$x$$;" +
         "  return function $f2$$($y$$) {" +
         "    return function $f3$$() {$x$$} }}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testNoShadowGlobalVariables
  public void testNoShadowGlobalVariables() {
    generatePseudoNames = true;
    test("var  x  ;  x  ; function  foo  () { return function ( y  ) {}}",
         "var $x$$; $x$$; function $foo$$() { return function ($y$$) {}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowBleedInFunctionName
  public void testShadowBleedInFunctionName() {
    generatePseudoNames = true;
    test("function  foo  () { function  b  ( y  ) { y  }  b  ;  b  ;}",
         "function $foo$$() { function $b$$($b$$) {$b$$} $b$$; $b$$;}");
   }

// com.google.javascript.jscomp.ShadowVariablesTest::testNoShadowLessPopularName
  public void testNoShadowLessPopularName() {
    generatePseudoNames = true;
    
    
    
    
    
    test("function  f1  ( x  ) {" +
         "  function  f2  ( y  ) {}  x  ; x  ;}" +
         "function  f3  ( i  ) {" +
         "  var  k  ; var  j  ; j  ; j  ; j  ; j  ; j  ; j  ;}",

         "function $f1$$($x$$) {" +
         "  function $f2$$($y$$) {} $x$$;$x$$;}" +
         "function $f3$$($i$$) {" +
         "  var $k$$; var $j$$;$j$$;$j$$;$j$$;$j$$;$j$$;$j$$;}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowFunctionName
  public void testShadowFunctionName() {
    generatePseudoNames = true;
    test("var  g   = function() {" +
         "  var  x  ; return function(){function  y  (){}}}",
         "var $g$$ = function() {" +
         "  var $x$$; return function(){function $x$$(){}}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowLotsOfScopes1
  public void testShadowLotsOfScopes1() {
    generatePseudoNames = true;
    test("var  g   = function( x  ) { return function() { return function() {" +
         " return function() { var  y   }}}}",
         "var $g$$ = function($x$$) { return function() { return function() {" +
         " return function() { var $x$$ }}}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowLotsOfScopes2
  public void testShadowLotsOfScopes2() {
    generatePseudoNames = true;
    
    test("var  g   = function( x  ) { return function( y  ) " +
         " {return function() {return function() {  x   }}}}",
         "var $g$$ = function($x$$) { return function($y$$) " +
         " {return function() {return function() { $x$$ }}}}");

    test("var  g   = function( x  ) { return function() " +
        " {return function( y  ) {return function() {  x   }}}}",
        "var $g$$ = function($x$$) { return function() " +
        " {return function($y$$) {return function() { $x$$ }}}}");

    test("var  g   = function( x  ) { return function() " +
        " {return function() {return function( y  ) {  x   }}}}",
        "var $g$$ = function($x$$) { return function() " +
        " {return function() {return function($y$$) { $x$$ }}}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowLotsOfScopes3
  public void testShadowLotsOfScopes3() {
    generatePseudoNames = true;
    
    test("var  g   = function( x  ) { return function() " +
        " {return function() {return function() {  x   }; var  y   }}}",
        "var $g$$ = function($x$$) { return function() " +
        " {return function() {return function() { $x$$ }; var $y$$}}}");
    test("var  g   = function( x  ) { return function() " +
        " {return function() {return function() {  x   }}; var  y   }}",
        "var $g$$ = function($x$$) { return function() " +
        " {return function() {return function() { $x$$ }}; var $y$$}}");
    test("var  g   = function( x  ) { return function() " +
        " {return function() {return function() {  x   }}}; var  y   }",
        "var $g$$ = function($x$$) { return function() " +
        " {return function() {return function() { $x$$ }}}; var $y$$}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowLotsOfScopes4
  public void testShadowLotsOfScopes4() {
    
    test("var g = function(x) { return function() { return function() {" +
         " return function(){return function(){};var m};var n};var o}}",
         "var b = function(a) { return function() { return function() {" +
         " return function(){return function(){};var a};var a};var a}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowLotsOfScopes5
  public void testShadowLotsOfScopes5() {
    generatePseudoNames = true;
    test("var  g   = function( x  ) {" +
         " return function() { return function() {" +
         " return function() { return function() {" +
         "      x  }; o  };var  n  };var  o  };var  p  }",
         "var $g$$ = function($x$$) {" +
         " return function() { return function() {" +
         " return function() { return function() {" +
         "     $x$$};$o$$};var $p$$};var $o$$};var $p$$}");

    test("var  g   = function( x  ) {" +
        " return function() { return function() {" +
        " return function() { return function() {" +
        "      x  }; p  };var  n  };var  o  };var  p  }",
        "var $g$$ = function($x$$) {" +
        " return function() { return function() {" +
        " return function() { return function() {" +
        "     $x$$};$p$$};var $o$$};var $o$$};var $p$$}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowWithShadowAlready
  public void testShadowWithShadowAlready() {
    test("var g = function(x) { return function() { return function() {" +
         " return function(){return function(){x}};var p};var o};var p}",
         "var c = function(b) { return function() { return function() {" +
         " return function(){return function(){b}};var a};var a};var a}");

    test("var g = function(x) { return function() { return function() {" +
         " return function(){return function(){x};p};var p};var o};var p}",
         "var c = function(b) { return function() { return function() {" +
         " return function(){return function(){b};a};var a};var a};var a}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowBug1
  public void testShadowBug1() {
    generatePseudoNames = true;
    test("function  f  ( x  ) { return function( y  ) {" +
         "    return function( x  ) {  x   +  y  ; }}}",
         "function $f$$($x$$) { return function($y$$) {" +
         "    return function($x$$) { $x$$ + $y$$; }}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testOptimal
  public void testOptimal() {
    
    test("function f(x) { function g(y) { function h(x) {}}}",
         "function c(a) { function b(a) { function b(a) {}}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testSharingAcrossInnerScopes
  public void testSharingAcrossInnerScopes() {
    test("function f() {var f=function g(){g()}; var x=function y(){y()}}",
         "function c() {var d=function a(){a()}; var e=function b(){b()}}");
    test("function f(x) { return x ? function(y){} : function(z) {} }",
         "function b(a) { return a ? function(a){} : function(a) {} }");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testExportedLocal1
  public void testExportedLocal1() {
    test("function f(a) { a();a();a(); return function($super){} }",
         "function b(a) { a();a();a(); return function($super){} }");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testExportedLocal2
  public void testExportedLocal2() {
    test("function f($super) { $super();$super(); return function(a){} }",
         "function a($super) { $super();$super(); return function(b){} }");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testBug4172539
  public void testBug4172539() {
    
    
    
    
    
    

    generatePseudoNames = true;
    test("function f(x) {" +
         "  x;x;x;" +
         "  return function (y) { y; x };" +
         "  return function (y) {" +
         "    y;" +
         "    return function (m, n) {" +
         "       m;m;m;" +
         "    };" +
         "  };" +
         "}",

         "function $f$$($x$$) {" +
         "  $x$$;$x$$;$x$$;" +
         "  return function ($y$$) { $y$$; $x$$ };" +
         "  return function ($x$$) {" +
         "    $x$$;" +
         "    return function ($x$$, $y$$) {" +
         "       $x$$;$x$$;$x$$;" +
         "    };" +
         "  };" +
         "}");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testDegenerateSafeMoves
  public void testDegenerateSafeMoves() {
    
    assertSafeMoveDegenerate("src: 1; env: ; dest: 3;");

    
    assertSafeMoveDegenerate("src: 1; env: 2; dest: 3;");

    
    assertSafeMoveDegenerate("src: 1; env: x; dest: 3;");
    assertSafeMoveDegenerate("src: x; env: 1; dest: 3;");

    
    assertSafeMoveDegenerate("src: 1; env: x++; dest: 3;");

    assertSafeMoveDegenerate("src: x++; env: 1; dest: 3;");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilitySafeMoves
  public void testVisibilitySafeMoves() {
    
    assertSafeMoveVisibility("src: 1; env: ; dest: 3;");

    
    assertSafeMoveVisibility("src: 1; env: 2; dest: 3;");

    
    assertSafeMoveVisibility("var x; src: 1; env: x; dest: 3;");
    assertSafeMoveVisibility("var x; src: x; env: 1; dest: 3;");

    
    assertSafeMoveVisibility("var x; src: 1; env: x++; dest: 3;");
    assertSafeMoveVisibility("var x; src: x++; env: 1; dest: 3;");

    
    assertSafeMoveVisibility(
        "var x;" +
        "function f(){" +
          "var y;" +
          "src: x;" +
          "env: y++;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "var x;" +
        "function f(){" +
          "var y;" +
          "src: x++;" +
          "env: y;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "var x;" +
        "var y;" +
        "function f(){" +
          "var y;" +
          "src: x;" +
          "env: y++;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "var x;" +
        "var y;" +
        "function f(){" +
          "var y;" +
          "src: x++;" +
          "env: y;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "function f(){" +
          "var x;" +
          "var y;" +
          "src: x;" +
          "env: y++;" +
          "dest: 3;" +
          "function inner() {" +
            "x" +
          "}" +
         "}");

    
    assertSafeMoveVisibility(
        "function f(){" +
          "var x;" +
          "var y;" +
          "src: x++;" +
          "env: y;" +
          "dest: 3;" +
          "function inner() {" +
            "x" +
          "}" +
        "}");

    
    assertSafeMoveVisibility(
        "var x = {};" +
        "function f(){" +
          "var y;" +
          "src: x.a;" +
          "env: y++;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "var x = {};" +
        "function f(){" +
          "var y;" +
          "src: x.a++;" +
          "env: y;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "var x = {};" +
        "src: x.a;" +
        "env: (function() {" +
          "x.a++;" +
        "});" +
        "dest: 3;");

    
    assertSafeMoveVisibility(
        "var x = {};" +
        "src: x.a++;" +
        "env: (function() {" +
          "x.a;" +
        "});" +
        "dest: 3;");

  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testDegenerateUnsafeMoves
  public void testDegenerateUnsafeMoves() {

    
    assertUnsafeMoveDegenerate("src: x++; env: foo(y); dest: 3;");

    
    assertUnsafeMoveDegenerate("src: foo(y); env: x++; dest: 3;");

    
    assertUnsafeMoveDegenerate("src: x = 7; env: y = 3; dest:3;");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilityUnsafeMoves
  public void testVisibilityUnsafeMoves() {

    
    assertUnsafeMoveVisibility("var x,y; src: x++; env: y; dest: 3;");

    
    assertUnsafeMoveVisibility("function f() {" +
        "var x,y; src: x++; env: y; dest: 3;" +
        "}");

    
    assertUnsafeMoveVisibility(
        "function f() {" +
          "var x,y; src: x++; env: y; dest: 3;" +
          "function inner() {" +
            "x; y;" +
          "}" +
         "}");

    
    assertUnsafeMoveVisibility("var x,y; src: x.a++; env: y.b; dest: 3;");

    
    assertUnsafeMoveVisibility("var x,y; src: y; env: x++; dest: 3;");

    
    assertUnsafeMoveVisibility("function f() {" +
        "var x,y; src: x; env: y++; dest: 3;" +
        "}");

    
    assertUnsafeMoveVisibility(
        "function f() {" +
          "var x,y; src: x; env: y++; dest: 3;" +
          "function inner() {" +
            "x; y;" +
          "}" +
         "}");

    
    assertUnsafeMoveVisibility("var x,y; src: x.a; env: y.b++; dest: 3;");

    
    assertUnsafeMoveVisibility("var x,y; src: x = 7; env: y = 3; dest: 3;");

    
    assertUnsafeMoveVisibility("function f() {" +
        "var x,y; src: x = 7; env: y = 3; dest: 3;" +
        "}");

    
    assertUnsafeMoveVisibility(
        "function f() {" +
          "var x,y; src: x = 7; env: y = 3; dest: 3;" +
          "function inner() {" +
            "x; y;" +
          "}" +
         "}");

    
    assertUnsafeMoveVisibility("var x,y; src: x.a = 7; env: y.b = 3; dest: 3;");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilityMoveCalls
  public void testVisibilityMoveCalls() {
    
    
    

    

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "var g = function(){};" +
        "function f(){" +
          "var y;" +
          "src: g();" +
          "env: x;" +
          "dest: 3;" +
          "}");

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "var g = function(){};" +
        "function f(){" +
          "var y;" +
          "src: x;" +
          "env: g();" +
          "dest: 3;" +
          "}");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilityMergesParametersWithHeap
  public void testVisibilityMergesParametersWithHeap() {
    
    
    

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: x[0]++;" +
          "env: y;" +
          "dest: 3;" +
          "}");

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: x[0];" +
          "env: y++;" +
          "dest: 3;" +
          "}");

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: arguments[0]++;" +
          "env: y;" +
          "dest: 3;" +
          "}");

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: arguments[0];" +
          "env: y++;" +
          "dest: 3;" +
          "}");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testMovedSideEffectsMustHaveSameControlFlow
  public void testMovedSideEffectsMustHaveSameControlFlow() {

    
    assertSafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "if (l) {" +
            "src: a++;" +
            "env: 3;" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "if (l) {" +
            "src: a++;" +
            "env: 3;" +
          "}" +
          "if (l) {" +
          "dest: 3;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "if (l) {" +
            "src: a++;" +
            "env: 3;" +
          "} else {" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertSafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: 3;" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: l;" +
            "break;" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: 3;" +
            "continue;" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: 3;" +
            "return;" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertSafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "do {" +
            "src: a++;" +
            "env: 3;" +
            "dest: 3;" +
          "} while(l)" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "do {" +
            "src: a++;" +
            "env: 3;" +
          "} while(l)" +
          "dest: 3;" +
        "}"
    );

    
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "switch(l) {" +
            "case 17:" +
              "src: a++;" +
              "env: 3;" +
              "dest: 3;" +
            "break;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "switch(l) {" +
            "case 17:" +
              "src: a++;" +
              "env: 3;" +
            "break;" +
            "case 18:" +
              "dest: 3;" +
            "break;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "src: a++;" +
          "env: 3;" +
        "}" +
        "function g() {" +
          "dest: 3;" +
        "}"
    );
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineNumber
  public void testDefineNumber() throws Exception {
    checkDefinitionsInJs(
        "var a = 1",
        ImmutableSet.of("DEF NAME a -> NUMBER"));

    checkDefinitionsInJs(
        "a = 1",
        ImmutableSet.of("DEF NAME a -> NUMBER"));

    checkDefinitionsInJs(
        "a.b = 1",
        ImmutableSet.of("DEF GETPROP a.b -> NUMBER"));

    
    checkDefinitionsInJs(
        "a[\"b\"] = 1",
        ImmutableSet.<String>of());

    checkDefinitionsInJs(
        "f().b = 1",
        ImmutableSet.of("DEF GETPROP null -> NUMBER"));

    checkDefinitionsInJs(
        "({a : 1}); o.a",
        ImmutableSet.of("DEF STRING null -> NUMBER",
                        "USE GETPROP o.a -> [NUMBER]"));

    
    checkDefinitionsInJs(
      "({'a' : 1}); o['a']",
      ImmutableSet.<String>of("DEF STRING null -> NUMBER"));

    checkDefinitionsInJs(
      "({1 : 1}); o[1]",
      ImmutableSet.<String>of("DEF STRING null -> NUMBER"));

    checkDefinitionsInJs(
        "var a = {b : 1}; a.b",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "DEF STRING null -> NUMBER",
                        "USE NAME a -> [<null>]",
                        "USE GETPROP a.b -> [NUMBER]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineGet
  public void testDefineGet() throws Exception {
    
    checkDefinitionsInJs(
      "({get a() {}}); o.a",
      ImmutableSet.of("DEF GET null -> FUNCTION",
                      "USE GETPROP o.a -> [FUNCTION]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineSet
  public void testDefineSet() throws Exception {
    
    checkDefinitionsInJs(
      "({set a(b) {}}); o.a",
      ImmutableSet.of("DEF NAME b -> <null>",
                      "DEF SET null -> FUNCTION",
                      "USE GETPROP o.a -> [FUNCTION]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineFunction
  public void testDefineFunction() throws Exception {
    checkDefinitionsInJs(
        "var a = function(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "var a = function f(){}",
        ImmutableSet.of("DEF NAME f -> FUNCTION", "DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "function a(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "a = function(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "a.b = function(){}",
        ImmutableSet.of("DEF GETPROP a.b -> FUNCTION"));

    
    checkDefinitionsInJs(
        "a[\"b\"] = function(){}",
        ImmutableSet.<String>of());

    checkDefinitionsInJs(
        "f().b = function(){}",
        ImmutableSet.of("DEF GETPROP null -> FUNCTION"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testFunctionArgumentsBasic
  public void testFunctionArgumentsBasic() throws Exception {
    checkDefinitionsInJs(
        "function f(a){return a}",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "USE NAME a -> [<null>]",
                        "DEF NAME f -> FUNCTION"));

    checkDefinitionsInJs(
        "var a = 1; function f(a){return a}",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME a -> <null>",
                        "USE NAME a -> [<null>, NUMBER]",
                        "DEF NAME f -> FUNCTION"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testFunctionArgumentsInExterns
  public void testFunctionArgumentsInExterns() throws Exception {
    final String DEF = "var f = function(arg1, arg2){}";
    final String USE = "f(1, 2)";

    
    checkDefinitionsInJs(
        DEF + ";" + USE,
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "DEF NAME arg1 -> <null>",
                        "DEF NAME arg2 -> <null>",
                        "USE NAME f -> [FUNCTION]"));

    
    checkDefinitions(
        DEF, USE,
        ImmutableSet.of("DEF NAME f -> EXTERN FUNCTION",
                        "USE NAME f -> [EXTERN FUNCTION]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testMultipleDefinition
  public void testMultipleDefinition() throws Exception {
    checkDefinitionsInJs(
        "a = 1; a = 2; a",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "USE NAME a -> [NUMBER x 2]"));

    checkDefinitionsInJs(
        "a = 1; a = 'a'; a",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME a -> STRING",
                        "USE NAME a -> [NUMBER, STRING]"));

    checkDefinitionsInJs(
        "a = 1; b = 2; a = b; a",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "DEF NAME a -> NUMBER",
                        "DEF NAME b -> NUMBER",
                        "USE NAME a -> [<null>, NUMBER]",
                        "USE NAME b -> [NUMBER]"));

    checkDefinitionsInJs(
        "a = 1; b = 2; c = b; c = a; c",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME b -> NUMBER",
                        "DEF NAME c -> <null>",
                        "USE NAME a -> [NUMBER]",
                        "USE NAME b -> [NUMBER]",
                        "USE NAME c -> [<null> x 2]"));

    checkDefinitionsInJs(
        "function f(){} f()",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.call(null)",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.call -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.apply(null, [])",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.apply -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.foobar()",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f(); f.call(null)",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.call -> [FUNCTION]"));

  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefinitionInExterns
  public void testDefinitionInExterns() throws Exception {
    String externs = "var a = 1";

    checkDefinitionsInExterns(
        externs,
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER"));

    checkDefinitions(
        externs,
        "var b = 1",
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER", "DEF NAME b -> NUMBER"));

    checkDefinitions(
        externs,
        "a = \"foo\"; a",
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER",
                        "DEF NAME a -> STRING",
                        "USE NAME a -> [EXTERN NUMBER, STRING]"));

    checkDefinitionsInExterns(
        "var a = {}; a.b = 10",
        ImmutableSet.of("DEF GETPROP a.b -> EXTERN NUMBER",
                        "DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitionsInExterns(
        "var a = {}; a.b",
        ImmutableSet.of("DEF GETPROP a.b -> EXTERN <null>",
                        "DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitions(
        "var a = {}",
        "a.b = 1",
        ImmutableSet.of("DEF GETPROP a.b -> NUMBER",
                        "DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitions(
        "var a = {}",
        "a.b",
        ImmutableSet.of("DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitionsInExterns(
        externs,
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testObjectLitInExterns
  public void testObjectLitInExterns() {
    checkDefinitions(
        "var goog = {};" +
        " goog.HYBRID;" +
        " goog.Enum = {HYBRID: 0, ROADMAP: 1};",
        "goog.HYBRID; goog.Enum.ROADMAP;",
        ImmutableSet.of(
            "DEF GETPROP goog.Enum -> EXTERN <null>",
            "DEF GETPROP goog.HYBRID -> EXTERN <null>",
            "DEF NAME goog -> EXTERN <null>",
            "DEF STRING null -> EXTERN NUMBER",
            "USE GETPROP goog.Enum -> [EXTERN <null>]",
            "USE GETPROP goog.Enum.ROADMAP -> [EXTERN NUMBER]",
            "USE GETPROP goog.HYBRID -> [EXTERN <null>, EXTERN NUMBER]",
            "USE NAME goog -> [EXTERN <null>]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testCallInExterns
  public void testCallInExterns() {
    checkDefinitionsInExterns(
        "var goog = {};" +
        " goog.Response = function() {};" +
        "goog.Response.prototype.get;" +
        "goog.Response.prototype.get().get;",
        ImmutableSet.of(
            "DEF GETPROP goog.Response -> EXTERN FUNCTION",
            "DEF GETPROP goog.Response.prototype.get -> EXTERN <null>",
            "DEF GETPROP null -> EXTERN <null>",
            "DEF NAME goog -> EXTERN <null>",
            "USE GETPROP goog.Response -> [EXTERN FUNCTION]",
            "USE GETPROP goog.Response.prototype.get -> [EXTERN <null> x 2]",
            "USE NAME goog -> [EXTERN <null>]"));
  }

// com.google.javascript.jscomp.SimpleFunctionAliasAnalysisTest::testFunctionGetIsAliased
  public void testFunctionGetIsAliased() {
    
    String source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "var D = function() {}\n" +
        "var aliasA = A;\n" +
        "var aliasB = ns.B;\n" +
        "var aliasC = C;\n" +
        "D();";

    compileAndRun(source);

    assertFunctionAliased(true, "A");
    assertFunctionAliased(true, "ns.B");
    assertFunctionAliased(true, "C");
    assertFunctionAliased(false, "D");

    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "ns.D = function() {}\n" +
        "var aliasA;\n" +
        "aliasA = A;\n" +
        "var aliasB = {};\n" +
        "aliasB.foo = ns.B;\n" +
        "var aliasC;\n" +
        "aliasC = C;\n" +
        "ns.D();";

    compileAndRun(source);

    assertFunctionAliased(true, "A");
    assertFunctionAliased(true, "ns.B");
    assertFunctionAliased(true, "C");
    assertFunctionAliased(false, "ns.D");

    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "function D() {}\n" +
        "var foo = function(a) {}\n" +
        "foo(A);\n" +
        "foo(ns.B)\n" +
        "foo(C);\n" +
        "D();";

    compileAndRun(source);

    assertFunctionAliased(true, "A");
    assertFunctionAliased(true, "ns.B");
    assertFunctionAliased(true, "C");
    assertFunctionAliased(false, "D");

    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "A();\n" +
        "ns.B();\n" +
        "C();\n";

    compileAndRun(source);

    assertFunctionAliased(false, "A");
    assertFunctionAliased(false, "ns.B");
    assertFunctionAliased(false, "C");

    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "A.foo;\n" +
        "ns.B.prototype;\n" +
        "C[0];\n";

    compileAndRun(source);

    assertFunctionAliased(false, "A");
    assertFunctionAliased(false, "ns.B");
    assertFunctionAliased(false, "C");
  }

// com.google.javascript.jscomp.SimpleFunctionAliasAnalysisTest::testFunctionGetIsExposedToCallOrApply
  public void testFunctionGetIsExposedToCallOrApply() {
    
    String source =
        "function A(){};\n" +
        "function B(){};\n" +
        "function C(){};\n" +
        "var x;\n" +
        "A.call(x);\n" +
        "B.apply(x);\n" +
        "C();\n";

    compileAndRun(source);

    assertFunctionExposedToCallOrApply(true, "A");
    assertFunctionExposedToCallOrApply(true, "B");
    assertFunctionExposedToCallOrApply(false, "C");

    source =
      "var ns = {};" +
      "ns.A = function(){};\n" +
      "ns.B = function(){};\n" +
      "ns.C = function(){};\n" +
      "var x;\n" +
      "ns.A.call(x);\n" +
      "ns.B.apply(x);\n" +
      "ns.C();\n";

    compileAndRun(source);

    assertFunctionExposedToCallOrApply(true, "ns.A");
    assertFunctionExposedToCallOrApply(true, "ns.B");
    assertFunctionExposedToCallOrApply(false, "ns.C");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement1
  public void testPrefixReplacement1() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("pre/","") );
    checkSourceMap2("", "pre/file1", "", "pre/file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"file1\",\"file2\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement2
    public void testPrefixReplacement2() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("pre/file","src") );
    checkSourceMap2("", "pre/file1", "", "pre/file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"src1\",\"src2\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement3
  public void testPrefixReplacement3() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("file1","x") );
    mappings.add( new SourceMap.LocationMapping("file2","y") );
    checkSourceMap2("", "file1", "", "file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"x\",\"y\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement4
  public void testPrefixReplacement4() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("file1","x") );
    mappings.add( new SourceMap.LocationMapping("file","y") );
    checkSourceMap2("", "file1", "", "file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"x\",\"y2\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInline
  public void testSpecializeInline() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return 6};" +
        "A();",
        
        "A();" +
        "B();" +
        "B = function() {return 7};" +
        "A();" +
        "B();"
        );

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "A();" +
        "B();" +
        "B = function() {return 7};" +
        "A();" +
        "B();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeCascadedInline
  public void testSpecializeCascadedInline() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return C()};" +
        "var C = function() {return 6};" +
        "A();",
        
        "B = function() {return 7};" +
    "A();");

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B, C;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return C()};" + 
        "C = function() {return 6};" + 
        "B = function() {return 7};" +
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithMultipleDependents
  public void testSpecializeInlineWithMultipleDependents() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return 6};" +
        "A();",
        
        "B = function() {return 7};" +
        "A();",
        
        "A();"
    );

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "B = function() {return 7};" +
        "A();",
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "A();",

    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithNamespaces
  public void testSpecializeInlineWithNamespaces() {
    JSModule[] modules = createModuleStar(
        
        "var ns = {};" +
        
        "ns.A = function() {alert(B());ns.A()};" +
        "var B = function() {return 6};" +
        "ns.A();",
        
        "B = function() {return 7};" +
    "ns.A();");

    test(modules, new String[] {
        
        "var ns = {};" +
        "ns.A = function() {alert(6);ns.A()};" + 
        "ns.A();" +
        "var B;",
        
        "ns.A = function() {alert(B());ns.A()};" + 
        "B = function() {return 6};" + 
        "B = function() {return 7};" +
        "ns.A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithRegularFunctions
  public void testSpecializeInlineWithRegularFunctions() {
    JSModule[] modules = createModuleStar(
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "A();",
        
        "B = function() {return 7};" +
    "A();");

    test(modules, new String[] {
        
        "function A() {alert(6);A()}" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        
        "B = function() {return 7};" +
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeLocalNonAnonymousFunctions
  public void testDontSpecializeLocalNonAnonymousFunctions() {
    
    enableNormalize(false);

    JSModule[] modules = createModuleStar(
        
        "(function(){var noSpecialize = " +
            "function() {alert(6)};noSpecialize()})()",
        
        "");

    test(modules, new String[] {
        
        "(function(){var noSpecialize = " +
            "function() {alert(6)};noSpecialize()})()",
        
        ""
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testAddDummyVarsForRemovedFunctions
  public void testAddDummyVarsForRemovedFunctions() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B() + C());A()};" +
        "var B = function() {return 6};" +
        "var C = function() {return 8};" +
        "A();",
        
        "" +
    "A();");

    test(modules, new String[] {
        
        "var A = function() {alert(6 + 8);A()};" + 
        "A();" +
        "var B, C;",
        
        "A = function() {alert(B() + C());A()};" + 
        "B = function() {return 6};" + 
        "C = function() {return 8};" + 
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeRemoveUnusedProperties
  public void testSpecializeRemoveUnusedProperties() {
    JSModule[] modules = createModuleStar(
        
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "Foo.prototype.c = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "var aliasA = Foo.prototype.a;" +
        "var x = new Foo();" +
        "x.a();",
        
        "Foo.prototype.b = function() {return 6};" +
        "Foo.prototype.c = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeAliasedFunctions_inline
  public void testDontSpecializeAliasedFunctions_inline() {
    JSModule[] modules = createModuleStar(
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "var aliasA = A;" +
        "A();",
        
        "B = function() {return 7};" +
        "B();");

    test(modules, new String[] {
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "var aliasA = A;" +
        "A();",
        
        "B = function() {return 7};" +
        "B();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeAliasedFunctions_remove_unused_properties
  public void testDontSpecializeAliasedFunctions_remove_unused_properties() {
    JSModule[] modules = createModuleStar(
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "var aliasB = Foo.prototype.b;" +
        "Foo.prototype.c = function() {return 7};" +
        "Foo.prototype.d = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();" +
        "var aliasC = (new Foo).c",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "var aliasB = Foo.prototype.b;" +
        "Foo.prototype.c = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();" +
        "var aliasC = (new Foo).c",
        
        "Foo.prototype.d = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeDevirtualizePrototypeMethods
  public void testSpecializeDevirtualizePrototypeMethods() {
    JSModule[] modules = createModuleStar(
        
        "" +
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a();return 7};" +
        "Foo.prototype.b = function() {this.a()};" +
        "var x = new Foo();" +
        "x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "var JSCompiler_StaticMethods_a =" +
              "function(JSCompiler_StaticMethods_a$self) {" +
           "JSCompiler_StaticMethods_a(JSCompiler_StaticMethods_a$self);" +
           "return 7" +
        "};" +
        "var x = new Foo();" +
        "JSCompiler_StaticMethods_a(x);",
        
        "Foo.prototype.a = function() {this.a();return 7};" +
        "Foo.prototype.b = function() {this.a()};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeDevirtualizePrototypeMethodsWithInline
  public void testSpecializeDevirtualizePrototypeMethodsWithInline() {
    JSModule[] modules = createModuleStar(
        
        "" +
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {return 7};" +
        "var x = new Foo();" +
        "var z = x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "var x = new Foo();" +
        "var z = 7;",
        
        "Foo.prototype.a = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testRemovedFunctions
    public void testRemovedFunctions() {
      testSame("function F(){}\nvar G = function(a){};");

      assertEquals(ImmutableSet.of(), lastState.getRemovedFunctions());

      Node functionF = findFunction("F");

      lastState.reportRemovedFunction(functionF, functionF.getParent());
      assertEquals(ImmutableSet.of(functionF), lastState.getRemovedFunctions());

      Node functionG = findFunction("F");

      lastState.reportRemovedFunction(functionG, functionF.getParent());
      assertEquals(ImmutableSet.of(functionF, functionG),
          lastState.getRemovedFunctions());

      assertEquals(ImmutableSet.of(), lastState.getSpecializedFunctions());
    }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializedFunctions
    public void testSpecializedFunctions() {
      testSame("function F(){}\nvar G = function(a){};");

      assertEquals(ImmutableSet.of(), lastState.getSpecializedFunctions());

      Node functionF = findFunction("F");

      lastState.reportSpecializedFunction(functionF);
      assertEquals(ImmutableSet.of(functionF),
          lastState.getSpecializedFunctions());

      Node functionG = findFunction("F");

      lastState.reportSpecializedFunction(functionG);
      assertEquals(ImmutableSet.of(functionF, functionG),
          lastState.getSpecializedFunctions());

      assertEquals(ImmutableSet.of(), lastState.getRemovedFunctions());
    }

// com.google.javascript.jscomp.SpecializeModuleTest::testCanFixupFunction
    public void testCanFixupFunction() {
      testSame("function F(){}\n" +
               "var G = function(a){};\n" +
               "var ns = {};" +
               "ns.H = function(){};" +
               "var ns2 = {I : function anon1(){}};" +
               "(function anon2(){})();");

      assertTrue(lastState.canFixupFunction(findFunction("F")));
      assertTrue(lastState.canFixupFunction(findFunction("G")));
      assertTrue(lastState.canFixupFunction(findFunction("ns.H")));
      assertFalse(lastState.canFixupFunction(findFunction("anon1")));
      assertFalse(lastState.canFixupFunction(findFunction("anon2")));

      
      testSame("function A(){}\n" +
          "var aliasA = A;\n");

      assertFalse(lastState.canFixupFunction(findFunction("A")));
    }

// com.google.javascript.jscomp.StatementFusionTest::testNothingToDo
  public void testNothingToDo() {
    fuseSame("");
    fuseSame("a");
    fuseSame("a()");
    fuseSame("if(a()){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFoldBlockWithStatements
  public void testFoldBlockWithStatements() {
    fuse("a;b;c", "a,b,c");
    fuse("a();b();c();", "a(),b(),c()");
    fuse("a(),b();c(),d()", "a(),b(),c(),d()");
    fuse("a();b(),c(),d()", "a(),b(),c(),d()");
    fuse("a(),b(),c();d()", "a(),b(),c(),d()");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFoldBlockIntoIf
  public void testFoldBlockIntoIf() {
    fuse("a;b;c;if(x){}", "if(a,b,c,x){}");
    fuse("a;b;c;if(x,y){}else{}", "if(a,b,c,x,y){}else{}");
    fuse("a;b;c;if(x,y){}", "if(a,b,c,x,y){}");
    fuse("a;b;c;if(x,y,z){}", "if(a,b,c,x,y,z){}");

    
    fuseSame("a();if(a()){}a()");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFoldBlockReturn
  public void testFoldBlockReturn() {
    fuse("a;b;c;return x", "return a,b,c,x");
    fuse("a;b;c;return x+y", "return a,b,c,x+y");

    
    fuseSame("a;b;c;return x;a;b;c");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFoldBlockThrow
  public void testFoldBlockThrow() {
    fuse("a;b;c;throw x", "throw a,b,c,x");
    fuse("a;b;c;throw x+y", "throw a,b,c,x+y");
    fuseSame("a;b;c;throw x;a;b;c");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFoldSwitch
  public void testFoldSwitch() {
    fuse("a;b;c;switch(x){}", "switch(a,b,c,x){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFuseIntoForIn
  public void testFuseIntoForIn() {
    fuse("a;b;c;for(x in y){}", "for(x in a,b,c,y){}");
    fuseSame("a();for(var x = b() in y){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testNoFuseIntoWhile
  public void testNoFuseIntoWhile() {
    fuseSame("a;b;c;while(x){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testNoFuseIntoDo
  public void testNoFuseIntoDo() {
    fuseSame("a;b;c;do{}while(x)");
  }

// com.google.javascript.jscomp.StatementFusionTest::testNoGlobalSchopeChanges
  public void testNoGlobalSchopeChanges() {
    testSame("a,b,c");
  }

// com.google.javascript.jscomp.StatementFusionTest::testNoFunctionBlockChanges
  public void testNoFunctionBlockChanges() {
    testSame("function foo() { a,b,c }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval
  public void testEval() {
    test("function foo() { eval('a'); }", null,
         StrictModeCheck.EVAL_USE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval2
  public void testEval2() {
    testSame("function foo(eval) {}",
         StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval3
  public void testEval3() {
    testSame("function foo() {} foo.eval = 3;");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval4
  public void testEval4() {
    testSame("function foo() { var eval = 3; }",
         StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval5
  public void testEval5() {
    testSame("function eval() {}", StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval6
  public void testEval6() {
    testSame("try {} catch (eval) {}", StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval7
  public void testEval7() {
    testSame("var o = {eval: 3};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval8
  public void testEval8() {
    testSame("var a; eval: while (true) { a = 3; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable
  public void testUnknownVariable() {
    testSame("function foo(a) { a = b; }", StrictModeCheck.UNKNOWN_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable2
  public void testUnknownVariable2() {
    testSame("a: while (true) { a = 3; }", StrictModeCheck.UNKNOWN_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable3
  public void testUnknownVariable3() {
    testSame("try {} catch (ex) { ex = 3; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments
  public void testArguments() {
    testSame("function foo(arguments) {}",
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments2
  public void testArguments2() {
    testSame("function foo() { var arguments = 3; }",
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments3
  public void testArguments3() {
    testSame("function arguments() {}",
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments4
  public void testArguments4() {
    testSame("try {} catch (arguments) {}",
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments5
  public void testArguments5() {
    testSame("var o = {arguments: 3};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEvalAssignment
  public void testEvalAssignment() {
    noCajaChecks = true;
    testSame("function foo() { eval = []; }",
         StrictModeCheck.EVAL_ASSIGNMENT);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEvalAssignment2
  public void testEvalAssignment2() {
    test("function foo() { eval = []; }", null, StrictModeCheck.EVAL_USE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testAssignToArguments
  public void testAssignToArguments() {
    testSame("function foo() { arguments = []; }",
         StrictModeCheck.ARGUMENTS_ASSIGNMENT);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteVar
  public void testDeleteVar() {
    testSame("var a; delete a", StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteFunction
  public void testDeleteFunction() {
    testSame("function a() {} delete a", StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteArgument
  public void testDeleteArgument() {
    testSame("function b(a) { delete a; }",
        StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteProperty
  public void testDeleteProperty() {
    testSame("function f(obj) { delete obj.a; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName
  public void testIllegalName() {
    test("var a__ = 3;", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName2
  public void testIllegalName2() {
    test("function a__() {}", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName3
  public void testIllegalName3() {
    test("function f(a__) {}", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName4
  public void testIllegalName4() {
    test("try {} catch (a__) {}", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName5
  public void testIllegalName5() {
    noVarCheck = true;
    test("var a = b__;", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName6
  public void testIllegalName6() {
    test("function f(obj) { return obj.a__; }", null,
         StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName7
  public void testIllegalName7() {
    noCajaChecks = true;
    testSame("var a__ = 3;");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName8
  public void testIllegalName8() {
    test("var o = {a__: 3};", null, StrictModeCheck.ILLEGAL_NAME);
    test("var o = {b: 3, a__: 4};", null, StrictModeCheck.ILLEGAL_NAME);
    test("var o = {b: 3, get a__() {}};", null, StrictModeCheck.ILLEGAL_NAME);
    test("var o = {b: 3, set a__(c) {}};", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName9
  public void testIllegalName9() {
    test("a__: while (true) { var b = 3; }", null,
         StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName10
  public void testIllegalName10() {
    
    testSame("var o = {1: 3, 2: 4};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDuplicateObjectLiteralKey
  public void testDuplicateObjectLiteralKey() {
    testSame("var o = {a: 1, b: 2, c: 3};");
    testSame("var x = { get a() {}, set a(p) {} };");

    testSame("var o = {a: 1, b: 2, a: 3};",
        StrictModeCheck.DUPLICATE_OBJECT_KEY);
    testSame("var x = { get a() {}, get a() {} };",
         StrictModeCheck.DUPLICATE_OBJECT_KEY);
    testSame("var x = { get a() {}, a: 1 };",
         StrictModeCheck.DUPLICATE_OBJECT_KEY);
    testSame("var x = { set a(p) {}, a: 1 };",
         StrictModeCheck.DUPLICATE_OBJECT_KEY);
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInConstructor
  public void testLoggerDefinedInConstructor() {
    test("a.b.c = function() {" +
         "  this.logger = goog.debug.Logger.getLogger('a.b.c');" +
         "};",
         "a.b.c=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype1
  public void testLoggerDefinedInPrototype1() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype.logger = goog.debug.Logger.getLogger('a.b.c');",
         "a.b.c=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype2
  public void testLoggerDefinedInPrototype2() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype = {logger: goog.debug.Logger.getLogger('a.b.c')}",
         "a.b.c = function() {};" +
         "a.b.c.prototype = {}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype3
  public void testLoggerDefinedInPrototype3() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype = { " +
         "  get logger() {return goog.debug.Logger.getLogger('a.b.c')}" +
         "}",
         "a.b.c = function() {};" +
         "a.b.c.prototype = {}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype4
  public void testLoggerDefinedInPrototype4() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype = { " +
         "  set logger(a) {this.x = goog.debug.Logger.getLogger('a.b.c')}" +
         "}",
         "a.b.c = function() {};" +
         "a.b.c.prototype = {}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype5
  public void testLoggerDefinedInPrototype5() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype = { " +
         "  get f() {return this.x;}," +
         "  set f(a) {this.x = goog.debug.Logger.getLogger('a.b.c')}" +
         "}",
         "a.b.c = function() {};" +
         "a.b.c.prototype = { " +
         "  get f() {return this.x;}," +
         "  set f(a) {this.x = null}" +
         "}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedStatically
  public void testLoggerDefinedStatically() {
    test("a.b.c = function() {};" +
         "a.b.c.logger = goog.debug.Logger.getLogger('a.b.c');",
         "a.b.c=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInObjectLiteral1
  public void testLoggerDefinedInObjectLiteral1() {
    test("a.b.c = {" +
         "  x: 0," +
         "  logger: goog.debug.Logger.getLogger('a.b.c')" +
         "};",
         "a.b.c={x:0}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInObjectLiteral2
  public void testLoggerDefinedInObjectLiteral2() {
    test("a.b.c = {" +
         "  x: 0," +
         "  get logger() {return goog.debug.Logger.getLogger('a.b.c')}" +
         "};",
         "a.b.c={x:0}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInObjectLiteral3
  public void testLoggerDefinedInObjectLiteral3() {
    test("a.b.c = {" +
         "  x: null," +
         "  get logger() {return this.x}," +
         "  set logger(a) {this.x  = goog.debug.Logger.getLogger(a)}" +
         "};",
         "a.b.c={x:null}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInObjectLiteral4
  public void testLoggerDefinedInObjectLiteral4() {
    test("a.b.c = {" +
         "  x: null," +
         "  get y() {return this.x}," +
         "  set y(a) {this.x  = goog.debug.Logger.getLogger(a)}" +
         "};",
         "a.b.c = {" +
         "  x: null," +
         "  get y() {return this.x}," +
         "  set y(a) {this.x  = null}" +
         "};");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototypeAndUsedInConstructor
  public void testLoggerDefinedInPrototypeAndUsedInConstructor() {
    test("a.b.c = function(level) {" +
         "  if (!this.logger.isLoggable(level)) {" +
         "    this.logger.setLevel(level);" +
         "  }" +
         "  this.logger.log(level, 'hi');" +
         "};" +
         "a.b.c.prototype.logger = goog.debug.Logger.getLogger('a.b.c');" +
         "a.b.c.prototype.go = function() { this.logger.finer('x'); };",
         "a.b.c=function(level){if(!null);};" +
         "a.b.c.prototype.go=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedStaticallyAndUsedInConstructor
  public void testLoggerDefinedStaticallyAndUsedInConstructor() {
    test("a.b.c = function(level) {" +
         "  if (!a.b.c.logger.isLoggable(level)) {" +
         "    a.b.c.logger.setLevel(level);" +
         "  }" +
         "  a.b.c.logger.log(level, 'hi');" +
         "};" +
         "a.b.c.logger = goog.debug.Logger.getLogger('a.b.c');",
         "a.b.c=function(level){if(!null);}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerVarDeclaration
  public void testLoggerVarDeclaration() {
    test("var logger = opt_logger || goog.debug.LogManager.getRoot();", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerMethodCallByVariableType
  public void testLoggerMethodCallByVariableType() {
    test("var x = goog.debug.Logger.getLogger('a.b.c'); y.info(a); x.info(a);",
         "y.info(a)");
  }

// com.google.javascript.jscomp.StripCodeTest::testSubPropertyAccessByVariableName
  public void testSubPropertyAccessByVariableName() {
    test("var x, y = goog.debug.Logger.getLogger('a.b.c');" +
         "var logger = x;" +
         "var curlevel = logger.level_ ? logger.getLevel().name : 3;",
         "var x;var curlevel=null?null:3");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrefixedVariableName
  public void testPrefixedVariableName() {
    test("this.blcLogger_ = goog.debug.Logger.getLogger('a.b.c');" +
         "this.blcLogger_.fine('Raised dirty states.');", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrefixedPropertyName
  public void testPrefixedPropertyName() {
    test("a.b.c.staticLogger_ = goog.debug.Logger.getLogger('a.b.c');" +
         "a.b.c.staticLogger_.fine('-' + a.b.c.d_())", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrefixedClassName
  public void testPrefixedClassName() {
    test("a.b.MyLogger = function(logger) {" +
         "  this.logger_ = logger;" +
         "};" +
         "a.b.MyLogger.prototype.shout = function(msg, opt_x) {" +
         "  this.logger_.log(goog.debug.Logger.Level.SHOUT, msg, opt_x);" +
         "};",
         "a.b.MyLogger=function(logger){};" +
         "a.b.MyLogger.prototype.shout=function(msg,opt_x){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerClassDefinition
  public void testLoggerClassDefinition() {
    test("goog.debug.Logger=function(name){this.name_=name}", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStaticLoggerPropertyDefinition
  public void testStaticLoggerPropertyDefinition() {
    test("goog.debug.Logger.Level.SHOUT=" +
         "new goog.debug.Logger.Level(x,1200)", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStaticLoggerMethodDefinition
  public void testStaticLoggerMethodDefinition() {
    test("goog.debug.Logger.getLogger=function(name){" +
         "return goog.debug.LogManager.getLogger(name)" +
         "};", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrototypeFieldDefinition
  public void testPrototypeFieldDefinition() {
    test("goog.debug.Logger.prototype.level_=null;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrototypeFieldDefinitionWithoutAssignment
  public void testPrototypeFieldDefinitionWithoutAssignment() {
    test("goog.debug.Logger.prototype.level_;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrototypeMethodDefinition
  public void testPrototypeMethodDefinition() {
    test("goog.debug.Logger.prototype.addHandler=" +
         "function(handler){this.handlers_.push(handler)};", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPublicPropertyAssignment
  public void testPublicPropertyAssignment() {
    
    
    
    testSame("rootLogger.someProperty=3");
    testSame("this.blcLogger_.level=x");
    testSame("goog.ui.Component.logger.prop=y");
  }

// com.google.javascript.jscomp.StripCodeTest::testGlobalCallWithStrippedType
  public void testGlobalCallWithStrippedType() {
    testSame("window.alert(goog.debug.Logger)");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType1
  public void testClassDefiningCallWithStripType1() {
    test("goog.debug.Logger.inherits(Object)", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType2
  public void testClassDefiningCallWithStripType2() {
    test("goog.formatter=function(){};" +
         "goog.inherits(goog.debug.Formatter,goog.formatter)",
         "goog.formatter=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType3
  public void testClassDefiningCallWithStripType3() {
    test("goog.formatter=function(){};" +
         "goog.inherits(goog.formatter,goog.debug.Formatter)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType4
  public void testClassDefiningCallWithStripType4() {
    test("goog.formatter=function(){};" +
         "goog.formatter.inherits(goog.debug.Formatter)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType5
  public void testClassDefiningCallWithStripType5() {
    testSame("goog.formatter=function(){};" +
             "goog.formatter.inherits(goog.debug.FormatterFoo)");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType6
  public void testClassDefiningCallWithStripType6() {
    test("goog.formatter=function(){};" +
         "goog.formatter.inherits(goog.debug.Formatter.Foo)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType7
  public void testClassDefiningCallWithStripType7() {
    test("goog.inherits(goog.debug.TextFormatter,goog.debug.Formatter)", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType8
  public void testClassDefiningCallWithStripType8() {
    
    test("goog.debug.DebugWindow = function(){}", "");
    test("goog.inherits(goog.debug.DebugWindow,Base)", "");

    
    
    testSame("goog.debug.DebugWindowFoo=function(){}");
    testSame("goog.inherits(goog.debug.DebugWindowFoo,Base)");
    testSame("goog.debug.DebugWindowFoo");
    testSame("goog.debug.DebugWindowFoo=1");

    
    test("goog.debug.DebugWindow.Foo=function(){}", "");
    test("goog.inherits(goog.debug.DebugWindow.Foo,Base)", "");
    test("goog.debug.DebugWindow.Foo", "");
    test("goog.debug.DebugWindow.Foo=1", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPropertyWithEmptyStringKey
  public void testPropertyWithEmptyStringKey() {
    test("goog.format.NUMERIC_SCALES_BINARY_ = {'': 1};",
         "goog.format.NUMERIC_SCALES_BINARY_={\"\":1}");
  }

// com.google.javascript.jscomp.StripCodeTest::testVarinIf
  public void testVarinIf() {
    test("if(x)var logger=null;else foo()", "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testGetElemInIf
  public void testGetElemInIf() {
    test("var logger=null;if(x)logger[f];else foo()", "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testAssignInIf
  public void testAssignInIf() {
    test("var logger=null;if(x)logger=1;else foo()",
         "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testNamePrefix
  public void testNamePrefix() {
    test("a = function(traceZZZ) {}; a.prototype.traceXXX = {x: 1};" +
         "a.prototype.z = function() { this.traceXXX.f(); };" +
         "var traceYYY = 0;",
         "a=function(traceZZZ){};a.prototype.z=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testTypePrefix
  public void testTypePrefix() {
    test("e.f.TraceXXX = function() {}; " +
         "e.f.TraceXXX.prototype.yyy = 2;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames
  public void testStripCallsToStrippedNames() {
    test("a = function() { this.logger_ = function(msg){}; };" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
    test("a = function() {};" +
         "a.prototype.logger_ = function(msg) {};" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripVarsInitializedFromStrippedNames
  public void testStripVarsInitializedFromStrippedNames() {
    test("a = function() { this.logger_ = function() { return 1; }; };" +
         "a.prototype.b = function() { " +
         "  var one = this.logger_(); if (one) foo() }",
          "a=function(){};a.prototype.b=function(){if(null)foo()}");
  }

// com.google.javascript.jscomp.StripCodeTest::testReportErrorOnStripInNestedAssignment
  public void testReportErrorOnStripInNestedAssignment() {
    
    test("(foo.logger_ = 7) + 8",
         "(foo.logger_ = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);

    
    test("(goog.debug.Logger.foo = 7) + 8",
         "(goog.debug.Logger.foo = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);

    
    test("(GA_GoogleDebugger.foo = 7) + 8",
         "(GA_GoogleDebugger.foo = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testNewOperatior1
  public void testNewOperatior1() {
    test("function foo() {} foo.bar = new goog.debug.Logger();",
         "function foo() {} foo.bar = null;");
  }

// com.google.javascript.jscomp.StripCodeTest::testNewOperatior2
  public void testNewOperatior2() {
    test("function foo() {} foo.bar = (new goog.debug.Logger()).foo();",
         "function foo() {} foo.bar = null;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting1
  public void testCrazyNesting1() {
    test("var x = {}; x[new goog.debug.Logger()] = 3;",
         "var x = {}; x[null] = 3;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting2
  public void testCrazyNesting2() {
    test("var x = {}; x[goog.debug.Logger.getLogger()] = 3;",
         "var x = {}; x[null] = 3;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting3
  public void testCrazyNesting3() {
    test("var x = function() {}; x(new goog.debug.Logger());",
         "var x = function() {}; x(null);");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting4
  public void testCrazyNesting4() {
    test("var x = function() {}; x(goog.debug.Logger.getLogger());",
         "var x = function() {}; x(null);");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting5
  public void testCrazyNesting5() {
    test("var x = function() {}; var y = {}; " +
         "var z = goog.debug.Logger.getLogger(); x(y[z['foo']]);",
         "var x = function() {}; var y = {}; x(y[null]);");
  }

// com.google.javascript.jscomp.TightenTypesTest::testTopLevelVariables
  public void testTopLevelVariables() {
    testSame(" function Foo() {}\n"
             + "var a = new Foo();\n"
             + "var b = a;\n");

    assertTrue(getType("Foo").isFunction());
    assertTrue(getType("a").isInstance());
    assertType("function (this:Foo): ()", getType("Foo"));
    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));

    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = new Foo();\n"
             + "a = new Bar();\n"
             + "var b = a;\n");

    assertTrue(getType("a").isUnion());
    assertType("(Bar,Foo)", getType("a"));
    assertType("Bar", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testNamespacedVariables
  public void testNamespacedVariables() {
    testSame("var goog = goog || {}; goog.foo = {};\n"
             + " goog.foo.Foo = function() {};\n"
             + "goog.foo.Foo.prototype.blah = function() {};\n"
             + " goog.foo.Bar = function() {};\n"
             + "goog.foo.Bar.prototype.blah = function() {};\n"
             + "function bar(a) { a.blah(); }\n"
             + "var baz = bar;\n"
             + "bar(new goog.foo.Foo);\n"
             + "baz(new goog.foo.Bar);\n");

    assertType("(goog.foo.Bar,goog.foo.Foo)", getParamType(getType("bar"), 0));
    assertType("(goog.foo.Bar,goog.foo.Foo)", getParamType(getType("baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testReturnSlot
  public void testReturnSlot() {
    testSame(" function Foo() {}\n"
             + "function bar() {\n"
             + "  var a = new Foo();\n"
             + "  return a;\n"
             + "}\n"
             + "var b = bar();\n");

    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testParameterSlots
  public void testParameterSlots() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "function bar(a, b) {}\n"
             + "bar(new Foo, new Foo);\n"
             + "bar(new Bar, null);\n");

    assertType("(Bar,Foo)", getParamType(getType("bar"), 0));
    assertType("Foo", getParamType(getType("bar"), 1));
    assertNull(getParamVar(getType("bar"), 2));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAliasedFunction
  public void testAliasedFunction() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "function bar(a) {}\n"
             + "var baz = bar;\n"
             + "bar(new Foo);\n"
             + "baz(new Bar);\n");

    assertType("(Bar,Foo)", getParamType(getType("bar"), 0));
    assertType("(Bar,Foo)", getParamType(getType("baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCatchStatement
  public void testCatchStatement() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
             " function Bar() {}\n"
             + "function bar() { try { } catch (e) { return e; } }\n"
             + " function ID10TError() {}\n"
             + "var a = bar(); throw new ID10TError();\n", null, null);

    assertType("(Error,EvalError,ID10TError,RangeError,ReferenceError,"
        + "SyntaxError,TypeError,URIError)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testConstructorParameterSlots
  public void testConstructorParameterSlots() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz(a) {}\n"
             + "new Baz(new Foo);\n"
             + "new Baz(new Bar);\n");

    assertType("(Bar,Foo)", getParamType(getType("Baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallSlot
  public void testCallSlot() {
    testSame("function foo() {}\n"
             + "function bar() {}\n"
             + "function baz() {}\n"
             + "var a = foo;\n"
             + "a = bar;\n"
             + "a();\n");

    assertTrue(isCalled(getType("foo")));
    assertTrue(isCalled(getType("bar")));
    assertFalse(isCalled(getType("baz")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testObjectLiteralTraversal
  public void testObjectLiteralTraversal() {
    testSame("var foo = function() {}\n"
             + "function bar() { return { 'a': foo()} };\n"
             + "bar();");
    assertTrue(isCalled(getType("foo")));
   }

// com.google.javascript.jscomp.TightenTypesTest::testThis
  public void testThis() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this; }\n"
             + "var a = new Foo();\n"
             + "var b = a.foo();\n");

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAssign
  public void testAssign() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = new Foo();\n"
             + "var b = a = new Bar();\n");

    assertType("(Bar,Foo)", getType("a"));
    assertType("Bar", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testComma
  public void testComma() {
    testSame(" function Foo() {b=new Foo()}\n"
             + "var b;"
             + " function Bar() {}\n"
             + "var a = (new Foo, new Bar);\n");

    assertType("Bar", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAnd
  public void testAnd() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = (new Foo && new Bar);\n");

    assertType("Bar", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testOr
  public void testOr() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " var f = new Foo();\n"
             + " var b = new Bar();\n"
             + "var a = (f || b);\n");

    assertType("(Bar,Foo)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testHook
  public void testHook() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = (1+1 == 2) ? new Foo : new Bar;\n");

    assertType("(Bar,Foo)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testFunctionLiteral
  public void testFunctionLiteral() {
    testSame(" function Foo() {}\n"
             + "var a = (function() { return new Foo; })();\n");

    assertType("Foo", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testNameLookup
  public void testNameLookup() {
    testSame(" function Foo() {}\n"
             + "var a = new Foo;\n"
             + "var b = (function() { return a; })();\n");

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetProp
  public void testGetProp() {
    testSame(" function Foo() {\n"
             + "  this.foo = new A();\n"
             + "}\n"
             + " function Bar() {\n"
             + "  this.foo = new B();\n"
             + "}\n"
             + " function Baz() {}\n"
             + " function A() {}\n"
             + " function B() {}\n"
             
             + " var foo = new Foo();\n"
             + " var bar = new Bar();\n"
             + " var baz = new Baz();\n" 
             + "var a = foo || bar || baz\n"
             + "var b = a.foo;\n");

    assertType("(A,B)", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetPrototypeProperty
  public void testGetPrototypeProperty() {
    testSame(" function Foo() {};\n"
             + " function Bar() {};\n"
             + "Bar.prototype.a = new Foo();\n"
             + "var a = Bar.prototype.a;\n");

    assertType("Foo", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetElem
  public void testGetElem() {
    testSame(""
             + "function Array(var_args) {}\n",
             " function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = [];\n"
             + "a[0] = new Foo;\n"
             + "a[1] = new Bar;\n"
             + "var b = a[0];\n"
             + "var c = [new Foo, new Bar];\n", null);

    assertType("Array", getType("a"));
    assertType("(Array,Bar,Foo)", getType("b"));
    assertType("Array", getType("c"));

    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz() {\n"
             + "  this.arr = [];\n"
             + "}\n"
             + "var b = new Baz;\n"
             + "b.arr[0] = new Foo;\n"
             + "b.arr[1] = new Bar;\n"
             + "var c = b.arr;\n");

    assertType("Array", getType("c"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetElem3
  public void testGetElem3() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
             " function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz() {\n"
             + "  this.arr = [];\n"
             + "}\n"
             + "function foo(anarr) {"
             + "}\n"
             + "var ar = [];\n"
             + "foo(ar);\n", null);

    assertType("Array", getType("ar"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testScopeDiscovery
  public void testScopeDiscovery() {
    testSame("function spam() {}\n"
             + "function foo() {}\n"
             + "function bar() {\n"
             + "  return function() { foo(); };\n"
             + "}"
             + "function baz() {\n"
             + "  return function() { bar()(); };\n"
             + "}"
             + "baz()()();\n");

    assertFalse(isCalled(getType("spam")));
    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSheqDiscovery
  public void testSheqDiscovery() {
    testSame("function spam() {}\n"
             + "\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo1 = function() { f1(); }\n"
             + "Foo.prototype.foo2 = function() { f2(); }\n"
             + "Foo.prototype.foo3 = function() { f3(); }\n"
             + "function baz(a) {\n"
             + "  a === null || a instanceof Foo ?\n"
             + "  Foo.prototype.foo1.call(this) :\n"
             + "  Foo.prototype.foo2.call(this);\n"
             + "}\n"
             + "function f1() {}\n"
             + "function f2() {}\n"
             + "function f3() {}\n"
             + "baz(3);\n");

    assertFalse(isCalled(getType("spam")));
    assertFalse(isCalled(getType("f3")));
    assertTrue(isCalled(getType("f1")));
    assertTrue(isCalled(getType("f2")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSubclass
  public void testSubclass() {
    testSame("\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this.bar; };\n"
             + "Foo.prototype.bar = function() { return new A(); };\n"
             + "\n"
             + "function Bar() {}\n"
             + "\n"
             + "Bar.prototype.bar = function() { return new B(); };\n"
             + " function A() {}\n"
             + " function B() {}\n"
             + "var a = (new Foo()).foo()();\n"
             + "a = (new Bar()).foo()();\n");

    ConcreteType fooType =
        getPropertyType(getFunctionPrototype(getType("Foo")), "foo");
    assertType("(Bar,Foo)", getThisType(fooType));
    assertType("(A,B)", getType("a"));

    testSame("\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this.bar; };\n"
             + "Foo.prototype.bar = function() { return new A(); };\n"
             + "\n"
             + "function Bar() {}\n"
             + "\n"
             + "Bar.prototype.bar = function() { return new B(); };\n"
             + " function A() {}\n"
             + " function B() {}\n"
             + "var a = (new Bar()).foo()();\n");

    fooType = getPropertyType(getFunctionPrototype(getType("Foo")), "foo");
    assertType("Bar", getThisType(fooType));
    assertType("B", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testArrayAssignments
  public void testArrayAssignments() {
    testSame(" function Foo() {}\n"
             + "var a = [];\n"
             + "function foo() { return []; }\n"
             + "(a.length == 0 ? a : foo())[0] = new Foo;\n"
             + "var b = a[0];\n"
             + "var c = foo()[0];\n");

    assertType("(Array,Foo)", getType("b"));
    assertType("(Array,Foo)", getType("c"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAllPropertyReference
  public void testAllPropertyReference() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.prop = function() { this.prop2(); }\n"
             + "Foo.prototype.prop2 = function() { b = new Foo; }\n"
             + "var a = new Foo;\n"
             + "a = [][0];\n"
             + "function fun(a) {\n"
             + "  return a.prop();\n"
             + "}\n"
             + "var b;\n"
             + "fun(a);\n"
             );

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallFunction
  public void testCallFunction() {
    testSame(" function Foo() { this.a = new A; }\n"
             + " function Bar() {\n"
             + "  Foo.call(this);\n"
             + "}\n"
             + " function A() {};\n"
             + "new Bar;");

    assertTrue(isCalled(getType("Foo")));
    assertTrue(isCalled(getType("A")));
    ConcreteType fooType = getThisType(getType("Foo"));
    assertType("A", getPropertyType(fooType, "a"));

    ConcreteType barType = getThisType(getType("Bar"));
    assertType("A", getPropertyType(barType, "a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallFunctionWithArgs
  public void testCallFunctionWithArgs() {
    testSame(" function Foo(o) { this.a = o; }\n"
             + " function Bar() {\n"
             + "  Foo.call(this, new A());\n"
             + "}\n"
             + " function A() {};\n"
             + "var b = new Bar;");

    assertTrue(isCalled(getType("Foo")));
    assertTrue(isCalled(getType("A")));

    ConcreteType barType = getThisType(getType("Bar"));
    assertType("A", getPropertyType(barType, "a"));

    ConcreteType fooType = getThisType(getType("Foo"));
    assertType("A", getPropertyType(fooType, "a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallPrototypeFunction
  public void testCallPrototypeFunction() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.a = function() { return new A; }\n"
             + "Foo.prototype.a = function() { return new A; };\n"
             + " function Bar() {}\n"
             + ""
             + "Bar.prototype.a = function() { return new B; };\n"
             + " function A() {};\n"
             + " function B() {};\n"
             + "var ret = Foo.prototype.a.call(new Bar);");

    assertType("A", getType("ret"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallPrototypeFunctionWithArgs
  public void testCallPrototypeFunctionWithArgs() {
    testSame(" function Foo() { this.p = null }\n"
             + "Foo.prototype.set = function(arg) { this.p = arg; };\n"
             + "Foo.prototype.get = function() { return this.p; };\n"
             + " function A() {};\n"
             + "Foo.prototype.set.call(new Foo, new A);\n"
             + "var ret = Foo.prototype.get.call(new Foo);");

    ConcreteType fooP = getFunctionPrototype(getType("Foo"));
    ConcreteFunctionType gFun = getPropertyType(fooP, "get").toFunction();
    ConcreteFunctionType sFun = getPropertyType(fooP, "set").toFunction();

    assertTrue(isCalled(sFun));
    assertTrue(isCalled(gFun));
    assertTrue(isCalled(getType("A")));
    assertType("A", getType("ret"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSetTimeout
  public void testSetTimeout() {
    testSame(" function Window() {};\n"
             + "Window.prototype.setTimeout = function(f, t) {};\n"
             + " var window;",
             " function A() {}\n"
             + "A.prototype.handle = function() { foo(); };\n"
             + "function foo() {}\n"
             + "window.setTimeout((new A).handle, 3);", null);

    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternType
  public void testExternType() {
    testSame(" function T() {};\n"
             + " function Ext() {};\n"
             + "\n"
             + "Ext.prototype.getT = function() {};\n"
             + " Ext.prototype.prop;\n"
             + " var ext;",
             "var b = ext.getT();\n"
             + "var p = ext.prop;", null);

    assertType("Ext", getType("ext"));
    assertType("T", getType("b"));
    assertType("T", getType("p"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternSubTypes
  public void testExternSubTypes() {
    testSame(" function A() {};\n"
             + " function B() {};\n"
             + " function C() {};\n"
             + " function D() {};\n"
             + " function Ext() {};\n"
             + " Ext.prototype.a;\n"
             + " Ext.prototype.b;\n"
             + " Ext.prototype.d;\n"
             + " Ext.prototype.getA = function() {};\n"
             + " Ext.prototype.getB = function() {};\n",
             "var a = (new Ext).a;\n"
             + "var a2 = (new Ext).getA();\n"
             + "var b = (new Ext).b;\n"
             + "var b2 = (new Ext).getB();\n"
             + "var d = (new Ext).d;\n", null);

    assertType("(A,B,C,D)", getType("a"));
    assertType("(A,B,C,D)", getType("a2"));
    assertType("(B,D)", getType("b"));
    assertType("(B,D)", getType("b2"));
    assertType("D", getType("d"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternSubTypesForObject
  public void testExternSubTypesForObject() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES
             + " function A() {};\n"
             + " function B() {};\n"
             + " "
             + "Object.prototype.eval = function(code) {};\n"
             + "\n"
             + "A.prototype.a;\n"
             + "\n"
             + "A.prototype.b = function(){};\n",
             "var a = (new A).b()", null, null);
    assertType("(A,ActiveXObject,Array,B,Boolean,Date,Error,EvalError,"
               + "Function,Number,Object,"
               + "RangeError,ReferenceError,RegExp,String,SyntaxError,"
               + "TypeError,URIError)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitPropCall
  public void testImplicitPropCall() {
    testSame(" function Window() {};\n"
             + "\n"
             + "Window.prototype.setTimeout = function(f, d) {};",
             "function foo() {};\n"
             + "(new Window).setTimeout(foo, 20);", null);

    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitPropCallWithArgs
  public void testImplicitPropCallWithArgs() {
    testSame(" function Window() {};\n"
             + " function EventListener() {};\n"
             + "\n"
             + "Window.prototype.addEventListener = function(t, f) {};\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Window).addEventListener('click', foo);", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testUntypedImplicitCallFromProperty
  public void testUntypedImplicitCallFromProperty() {
    testSame(" function Element() {};\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};"
             + " Event.prototype.erv;",
             " function foo(evt) { return bar(evt); };\n"
             + "function bar(a) { return a.type() }\n"
             + " var ar = new Element;\n"
             + "ar.onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertTrue(isCalled(getType("bar")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Event", getParamType(getType("bar"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromProperty
  public void testImplicitCallFromProperty() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Element).onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromPropertyOfUnion
  public void testImplicitCallFromPropertyOfUnion() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Element).onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromPropertyOfAllType
  public void testImplicitCallFromPropertyOfAllType() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "var elems = [];\n"
             + "var elem = elems[0];\n" 
             + "elem.onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToCast
  public void testRestrictToCast() {
    testSame(" function Foo() {};\n"
             + "var a = [];\n"
             + "var foo = ( a[0]);\n"
             + "var u = a[0];\n"
             + "new Foo");

    assertType("Foo", getType("foo"));
    assertType("(Array,Foo)", getType("u"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToInterfaceCast
  public void testRestrictToInterfaceCast() {
    testSame(" function Foo() {};\n"
             + " function Int() {};\n"
             + "var a = [];\n"
             + "var foo = ( a[0]);\n"
             + "new Foo");

    assertType("Foo", getType("foo"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToCastWithNonInstantiatedTypes
  public void testRestrictToCastWithNonInstantiatedTypes() {
    testSame(
             " function Super() {}\n"
             + " function Foo() {};\n"
             + "Foo.prototype.blah = function() { foofunc() };\n"
             + " function Bar() {};\n"
             + "Bar.prototype.blah = function() { barfunc() };\n"
             + "function barfunc() {}\n"
             + "function foofunc() {}\n"
             + "var a = [];\n"
             + "var u =  (a[0]);\n"
             + "u.blah()\n"
             + "new Foo");

    assertTrue(isCalled(getType("foofunc")));
    assertFalse(isCalled(getType("barfunc")));
    assertType("Array", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testFunctionToString
  public void testFunctionToString() {
    testSame(" function Foo() {}\n"
             + "\n"
             + "function Bar() { Foo.call(this); }\n"
             + "var a = function(a) { return new Foo; };\n;"
             + "a(new Foo);\n"
             + "a(new Bar);\n"
             + "new Bar;");

    assertType("function ((Bar,Foo)): Foo", getType("a"));
    assertType("function (this:(Bar,Foo)): ()", getType("Foo"));
    assertType("function (this:Bar): ()", getType("Bar"));
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionAritySimple
  public void testFunctionAritySimple() {
    assertOk("", "");
    assertOk("a", "'a'");
    assertOk("a,b", "10, 20");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionArityWithOptionalArgs
  public void testFunctionArityWithOptionalArgs() {
    assertOk("a,b,opt_c", "1,2");
    assertOk("a,b,opt_c", "1,2,3");
    assertOk("a,opt_b,opt_c", "1");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionArityWithVarArgs
  public void testFunctionArityWithVarArgs() {
    assertOk("var_args", "");
    assertOk("var_args", "1,2");
    assertOk("a,b,var_args", "1,2");
    assertOk("a,b,var_args", "1,2,3");
    assertOk("a,b,var_args", "1,2,3,4,5");
    assertOk("a,opt_b,var_args", "1");
    assertOk("a,opt_b,var_args", "1,2");
    assertOk("a,opt_b,var_args", "1,2,3");
    assertOk("a,opt_b,var_args", "1,2,3,4,5");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testWrongNumberOfArgs
  public void testWrongNumberOfArgs() {
    assertWarning("a,b,opt_c", "1",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,opt_c", "1,2,3,4",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,c,d", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,var_args", "1",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,opt_c,var_args", "1",
        WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testVarArgsLast
  public void testVarArgsLast() {
    assertWarning("a,b,var_args,c", "1,2,3,4",
        VAR_ARGS_MUST_BE_LAST);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testOptArgsLast
  public void testOptArgsLast() {
    assertWarning("a,b,opt_d,c", "1, 2, 3",
        OPTIONAL_ARG_AT_END);
    assertWarning("a,b,opt_d,c", "1, 2",
        OPTIONAL_ARG_AT_END);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc1
  public void testFunctionsWithJsDoc1() {
    testSame(" function foo(a,b,c) {} foo(1,2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc2
  public void testFunctionsWithJsDoc2() {
    testSame(" function foo(a,b,c) {} foo(1,2,3);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc3
  public void testFunctionsWithJsDoc3() {
    testSame(" " +
             "function foo(a,b,c) {} foo(1);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc4
  public void testFunctionsWithJsDoc4() {
    testSame(" var foo = function(a) {}; foo();");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc5
  public void testFunctionsWithJsDoc5() {
    testSame(" var foo = function(a) {}; foo(1,2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc6
  public void testFunctionsWithJsDoc6() {
    testSame(" var foo = function(a, b) {}; foo();",
             WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc7
  public void testFunctionsWithJsDoc7() {
    String fooDfn = " var foo = function(b) {};";
    testSame(fooDfn + "foo();");
    testSame(fooDfn + "foo(1);");
    testSame(fooDfn + "foo(1, 2);", WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionWithDefaultCodingConvention
  public void testFunctionWithDefaultCodingConvention() {
    convention = CodingConventions.getDefault();
    testSame("var foo = function(x) {}; foo(1, 2);");
    testSame("var foo = function(opt_x) {}; foo(1, 2);");
    testSame("var foo = function(var_args) {}; foo(1, 2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testMethodCalls
  public void testMethodCalls() {
    final String METHOD_DEFS =
      "\n" +
      "function Foo() {}" +
      
      "function twoArg(arg1, arg2) {};" +
      "Foo.prototype.prototypeMethod = twoArg;" +
      "Foo.staticMethod = twoArg;" +
      
      "\n" +
      "function Bar() {}";

    
    testSame(METHOD_DEFS +
        "var f = new Foo();f.prototypeMethod(1, 2, 3);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
    
    testSame(METHOD_DEFS +
        "var f = new Foo();f.prototypeMethod(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);

    
    testSame(METHOD_DEFS +
        "Foo.staticMethod(1, 2, 3);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
    
    testSame(METHOD_DEFS +
        "Foo.staticMethod(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);

    
    testSame(METHOD_DEFS + "Bar();", TypeCheck.CONSTRUCTOR_NOT_CALLABLE);

    
    testSame(METHOD_DEFS, "Foo();", TypeCheck.CONSTRUCTOR_NOT_CALLABLE);

    
    testSame(METHOD_DEFS, "Bar();", null);
  }

// com.google.javascript.jscomp.TypeValidatorTest::testBasicMismatch
  public void testBasicMismatch() throws Exception {
    testSame(" function f(x) {} f('a');",
        TYPE_MISMATCH_WARNING);
    assertMismatches(Lists.newArrayList(fromNatives(STRING_TYPE, NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.TypeValidatorTest::testFunctionMismatch
  public void testFunctionMismatch() throws Exception {
    testSame(
        " function f(x) { return x; }",
        TYPE_MISMATCH_WARNING);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType string = registry.getNativeType(STRING_TYPE);
    JSType bool = registry.getNativeType(BOOLEAN_TYPE);
    JSType number = registry.getNativeType(NUMBER_TYPE);
    JSType firstFunction = registry.createFunctionType(number, string);
    JSType secondFunction = registry.createFunctionType(string, bool);

    assertMismatches(
        Lists.newArrayList(
            new TypeMismatch(firstFunction, secondFunction, null),
            fromNatives(STRING_TYPE, BOOLEAN_TYPE),
            fromNatives(NUMBER_TYPE, STRING_TYPE)));
  }

// com.google.javascript.jscomp.TypeValidatorTest::testFunctionMismatch2
  public void testFunctionMismatch2() throws Exception {
    testSame(
        " function f(x) { return x; }",
        TYPE_MISMATCH_WARNING);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType string = registry.getNativeType(STRING_TYPE);
    JSType bool = registry.getNativeType(BOOLEAN_TYPE);
    JSType number = registry.getNativeType(NUMBER_TYPE);
    JSType firstFunction = registry.createFunctionType(number, string);
    JSType secondFunction = registry.createFunctionType(number, bool);

    assertMismatches(
        Lists.newArrayList(
            new TypeMismatch(firstFunction, secondFunction, null),
            fromNatives(STRING_TYPE, BOOLEAN_TYPE)));
  }

// com.google.javascript.jscomp.TypeValidatorTest::testNullUndefined
  public void testNullUndefined() {
    testSame(" function f(x) {}\n" +
             "f( ('a'));",
             TYPE_MISMATCH_WARNING);
    assertMismatches(Collections.<TypeMismatch>emptyList());
  }

// com.google.javascript.jscomp.TypeValidatorTest::testSubclass
  public void testSubclass() {
    testSame("\n"  +
             "function Super() {}\n" +
             "\n" +
             "function Sub() {}\n" +
             " function f(x) {}\n" +
             "f( (new Sub));",
             TYPE_MISMATCH_WARNING);
    assertMismatches(Collections.<TypeMismatch>emptyList());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubProperty
  public void testStubProperty() {
    testSame("function Foo() {}; Foo.bar;");
    ObjectType foo = (ObjectType) globalScope.getVar("Foo").getType();
    assertFalse(foo.hasProperty("bar"));
    assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        foo.getPropertyType("bar"));
    assertEquals(Lists.newArrayList(foo), registry.getTypesWithProperty("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorProperty
  public void testConstructorProperty() {
    testSame("var foo = {};  foo.Bar = function() {};");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("Bar"));
    assertFalse(foo.isPropertyTypeInferred("Bar"));

    JSType fooBar = foo.getPropertyType("Bar");
    assertEquals("function (new:foo.Bar): undefined", fooBar.toString());
    assertEquals(Lists.newArrayList(foo), registry.getTypesWithProperty("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnumProperty
  public void testEnumProperty() {
    testSame("var foo = {};  foo.Bar = {XXX: 'xxx'};");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("Bar"));
    assertFalse(foo.isPropertyTypeInferred("Bar"));
    assertTrue(foo.isPropertyTypeDeclared("Bar"));

    JSType fooBar = foo.getPropertyType("Bar");
    assertEquals("enum{foo.Bar}", fooBar.toString());
    assertEquals(Lists.newArrayList(foo), registry.getTypesWithProperty("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty1
  public void testInferredProperty1() {
    testSame("var foo = {}; foo.Bar = 3;");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertTrue(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty1a
  public void testInferredProperty1a() {
    testSame("var foo = {};  foo.Bar = 3;");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertFalse(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty2
  public void testInferredProperty2() {
    testSame("var foo = { Bar: 3 };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertTrue(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty2b
  public void testInferredProperty2b() {
    testSame("var foo = {  Bar: 3 };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertFalse(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty2c
  public void testInferredProperty2c() {
    testSame("var foo = {  Bar: 3 };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("function (): number", foo.getPropertyType("Bar").toString());
    assertFalse(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty3
  public void testInferredProperty3() {
    testSame("var foo = {  get Bar() { return 3 } };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("?", foo.getPropertyType("Bar").toString());
    assertTrue(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty4
  public void testInferredProperty4() {
    testSame("var foo = {  set Bar(a) {} };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("?", foo.getPropertyType("Bar").toString());
    assertTrue(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty5
  public void testInferredProperty5() {
    testSame("var foo = {  get Bar() { return 3 } };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertFalse(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty6
  public void testInferredProperty6() {
    testSame("var foo = {  set Bar(a) {} };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertFalse(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPrototypeInit
  public void testPrototypeInit() {
    testSame(" var Foo = function() {};" +
        "Foo.prototype = {bar: 1}; var foo = new Foo();");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("bar"));
    assertEquals("number", foo.getPropertyType("bar").toString());
    assertTrue(foo.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredPrototypeProperty1
  public void testInferredPrototypeProperty1() {
    testSame(" var Foo = function() {};" +
        "Foo.prototype.bar = 1; var x = new Foo();");

    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertTrue(x.hasProperty("bar"));
    assertEquals("number", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredPrototypeProperty2
  public void testInferredPrototypeProperty2() {
    testSame(" var Foo = function() {};" +
        "Foo.prototype = {bar: 1}; var x = new Foo();");

    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertTrue(x.hasProperty("bar"));
    assertEquals("number", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnum
  public void testEnum() {
    testSame(" var Foo = {BAR: 1}; var f = Foo;");
    ObjectType f = (ObjectType) findNameType("f", globalScope);
    assertTrue(f.hasProperty("BAR"));
    assertEquals("Foo.<number>", f.getPropertyType("BAR").toString());
    assertTrue(f instanceof EnumType);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedEnum
  public void testNamespacedEnum() {
    testSame("var goog = {}; goog.ui = {};" +
        "goog.ui.Zippy = function() {};" +
        "goog.ui.Zippy.EventType = { TOGGLE: 'toggle' };" +
        "var x = goog.ui.Zippy.EventType;" +
        "var y = goog.ui.Zippy.EventType.TOGGLE;");

    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertTrue(x.isEnumType());
    assertTrue(x.hasProperty("TOGGLE"));
    assertEquals("enum{goog.ui.Zippy.EventType}", x.getReferenceName());

    ObjectType y = (ObjectType) findNameType("y", globalScope);
    assertTrue(y.isSubtype(getNativeType(STRING_TYPE)));
    assertTrue(y.isEnumElementType());
    assertEquals("goog.ui.Zippy.EventType", y.getReferenceName());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnumAlias
  public void testEnumAlias() {
    testSame(" var Foo = {BAR: 1}; " +
        " var FooAlias = Foo; var f = FooAlias;");

    assertEquals("Foo.<number>",
        registry.getType("FooAlias").toString());
    assertEquals(registry.getType("FooAlias"),
        registry.getType("Foo"));

    ObjectType f = (ObjectType) findNameType("f", globalScope);
    assertTrue(f.hasProperty("BAR"));
    assertEquals("Foo.<number>", f.getPropertyType("BAR").toString());
    assertTrue(f instanceof EnumType);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacesEnumAlias
  public void testNamespacesEnumAlias() {
    testSame("var goog = {};  goog.Foo = {BAR: 1}; " +
        " goog.FooAlias = goog.Foo;");

    assertEquals("goog.Foo.<number>",
        registry.getType("goog.FooAlias").toString());
    assertEquals(registry.getType("goog.Foo"),
        registry.getType("goog.FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testCollectedFunctionStub
  public void testCollectedFunctionStub() {
    testSame(
        " function f() { " +
        "   this.foo;" +
        "}" +
        "var x = new f();");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("f", x.toString());
    assertTrue(x.hasProperty("foo"));
    assertEquals("function (this:f): number",
        x.getPropertyType("foo").toString());
    assertFalse(x.isPropertyTypeInferred("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testCollectedFunctionStubLocal
  public void testCollectedFunctionStubLocal() {
    testSame(
        "(function() {" +
        " function f() { " +
        "   this.foo;" +
        "}" +
        "var x = new f();" +
        "});");
    ObjectType x = (ObjectType) findNameType("x", lastLocalScope);
    assertEquals("f", x.toString());
    assertTrue(x.hasProperty("foo"));
    assertEquals("function (this:f): number",
        x.getPropertyType("foo").toString());
    assertFalse(x.isPropertyTypeInferred("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedFunctionStub
  public void testNamespacedFunctionStub() {
    testSame(
        "var goog = {};" +
        " goog.foo;");

    ObjectType goog = (ObjectType) findNameType("goog", globalScope);
    assertTrue(goog.hasProperty("foo"));
    assertEquals("function (number): ?",
        goog.getPropertyType("foo").toString());
    assertTrue(goog.isPropertyTypeDeclared("foo"));

    assertEquals(globalScope.getVar("goog.foo").getType(),
        goog.getPropertyType("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedFunctionStubLocal
  public void testNamespacedFunctionStubLocal() {
    testSame(
        "(function() {" +
        "var goog = {};" +
        " goog.foo;" +
        "});");

    ObjectType goog = (ObjectType) findNameType("goog", lastLocalScope);
    assertTrue(goog.hasProperty("foo"));
    assertEquals("function (number): ?",
        goog.getPropertyType("foo").toString());
    assertTrue(goog.isPropertyTypeDeclared("foo"));

    assertEquals(lastLocalScope.getVar("goog.foo").getType(),
        goog.getPropertyType("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testCollectedCtorProperty
  public void testCollectedCtorProperty() {
    testSame(
        " function f() { " +
        "   this.foo = 3;" +
        "}" +
        "var x = new f();");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("f", x.toString());
    assertTrue(x.hasProperty("foo"));
    assertEquals("number", x.getPropertyType("foo").toString());
    assertFalse(x.isPropertyTypeInferred("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyOnUnknownSuperClass1
  public void testPropertyOnUnknownSuperClass1() {
    testSame(
        "var goog = this.foo();" +
        "" +
        "function Foo() {}" +
        "Foo.prototype.bar = 1;" +
        "var x = new Foo();",
        RhinoErrorReporter.TYPE_PARSE_ERROR);
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Foo", x.toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("bar"));
    assertEquals("?", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyOnUnknownSuperClass2
  public void testPropertyOnUnknownSuperClass2() {
    testSame(
        "var goog = this.foo();" +
        "" +
        "function Foo() {}" +
        "Foo.prototype = {bar: 1};" +
        "var x = new Foo();",
        RhinoErrorReporter.TYPE_PARSE_ERROR);
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Foo", x.toString());
    assertEquals("Foo.prototype", x.getImplicitPrototype().toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("bar"));
    assertEquals("?", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testMethodBeforeFunction1
  public void testMethodBeforeFunction1() throws Exception {
    testSame(
        "var y = Window.prototype;" +
        "Window.prototype.alert = function(message) {};" +
        " function Window() {}\n" +
        "var window = new Window(); \n" +
        "var x = window;");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Window", x.toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("alert"));
    assertEquals("function (this:Window, ?): undefined",
        x.getPropertyType("alert").toString());
    assertTrue(x.isPropertyTypeDeclared("alert"));

    ObjectType y = (ObjectType) findNameType("y", globalScope);
    assertEquals("function (this:Window, ?): undefined",
        y.getPropertyType("alert").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testMethodBeforeFunction2
  public void testMethodBeforeFunction2() throws Exception {
    testSame(
        "var y = Window.prototype;" +
        "Window.prototype = {alert: function(message) {}};" +
        " function Window() {}\n" +
        "var window = new Window(); \n" +
        "var x = window;");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Window", x.toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("alert"));
    assertEquals("function (this:Window, ?): undefined",
        x.getPropertyType("alert").toString());
    assertFalse(x.isPropertyTypeDeclared("alert"));

    ObjectType y = (ObjectType) findNameType("y", globalScope);
    assertEquals("?",
        y.getPropertyType("alert").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAddMethodsPrototypeTwoWays
  public void testAddMethodsPrototypeTwoWays() throws Exception {
    testSame(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true};" +
        "A.prototype.m3 = 'third property!';" +
        "var x = new A();");

    ObjectType instanceType = (ObjectType) findNameType("x", globalScope);
    assertEquals(
        getNativeObjectType(OBJECT_TYPE).getPropertiesCount() + 3,
        instanceType.getPropertiesCount());
    assertEquals(getNativeType(NUMBER_TYPE),
        instanceType.getPropertyType("m1"));
    assertEquals(getNativeType(BOOLEAN_TYPE),
        instanceType.getPropertyType("m2"));
    assertEquals(getNativeType(STRING_TYPE),
        instanceType.getPropertyType("m3"));

    
    
    
    assertFalse(instanceType.hasOwnProperty("m1"));
    assertFalse(instanceType.hasOwnProperty("m2"));
    assertFalse(instanceType.hasOwnProperty("m3"));

    ObjectType proto1 = instanceType.getImplicitPrototype();
    assertTrue(proto1.hasOwnProperty("m1"));
    assertTrue(proto1.hasOwnProperty("m2"));
    assertTrue(proto1.hasOwnProperty("m3"));

    ObjectType proto2 = proto1.getImplicitPrototype();
    assertFalse(proto2.hasProperty("m1"));
    assertFalse(proto2.hasProperty("m2"));
    assertFalse(proto2.hasProperty("m3"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredVar
  public void testInferredVar() throws Exception {
    testSame("var x = 3; x = 'x'; x = true;");

    Var x = globalScope.getVar("x");
    assertEquals("(boolean|number|string)", x.getType().toString());
    assertTrue(x.isTypeInferred());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredVar
  public void testDeclaredVar() throws Exception {
    testSame(" var x = 3; var y = x;");

    Var x = globalScope.getVar("x");
    assertEquals("(null|number)", x.getType().toString());
    assertFalse(x.isTypeInferred());

    JSType y = findNameType("y", globalScope);
    assertEquals("(null|number)", y.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertiesOnInterface
  public void testPropertiesOnInterface() throws Exception {
    testSame(" var I = function() {};" +
        " I.prototype.bar;" +
        "I.prototype.baz = function(){};");

    Var i = globalScope.getVar("I");
    assertEquals("function (this:I): ?", i.getType().toString());
    assertTrue(i.getType().isInterface());

    ObjectType iPrototype = (ObjectType)
        ((ObjectType) i.getType()).getPropertyType("prototype");
    assertEquals("I.prototype", iPrototype.toString());
    assertTrue(iPrototype.isFunctionPrototypeType());

    assertEquals("number", iPrototype.getPropertyType("bar").toString());
    assertEquals("function (this:I): undefined",
        iPrototype.getPropertyType("baz").toString());

    assertEquals(iPrototype, globalScope.getVar("I.prototype").getType());
  }
