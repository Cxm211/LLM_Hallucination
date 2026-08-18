// buggy code
    private void handleObjectLit(NodeTraversal t, Node n) {
      for (Node child = n.getFirstChild();
          child != null;
          child = child.getNext()) {
        // Maybe STRING, GET, SET

        // We should never see a mix of numbers and strings.
        String name = child.getString();
        T type = typeSystem.getType(getScope(), n, name);

        Property prop = getProperty(name);
        if (!prop.scheduleRenaming(child,
                                   processProperty(t, prop, type, null))) {
          // TODO(user): It doesn't look like the user can do much in this
          // case right now.
          if (propertiesToErrorFor.containsKey(name)) {
            compiler.report(JSError.make(
                t.getSourceName(), child, propertiesToErrorFor.get(name),
                Warnings.INVALIDATION, name,
                (type == null ? "null" : type.toString()), n.toString(), ""));
          }
        }
      }
    }

// relevant test
// com.google.javascript.jscomp.RhinoErrorReporterTest::testMisplacedTypeAnnotation
  public void testMisplacedTypeAnnotation() throws Exception {
    reportMisplacedTypeAnnotations = false;

    assertNoWarningOrError("var x =  y;");

    reportMisplacedTypeAnnotations = true;

    String message =
        "Type annotations are not allowed here. " +
        "Are you missing parentheses?";
    JSError error = assertWarning(
        "var x =  y;",
        RhinoErrorReporter.MISPLACED_TYPE_ANNOTATION,
        message);

    assertEquals(1, error.getLineNumber());
    assertEquals(0, error.getCharno());
  }

// com.google.javascript.jscomp.RhinoErrorReporterTest::testInvalidEs3Prop
  public void testInvalidEs3Prop() throws Exception {
    reportEs3Props = false;

    assertNoWarningOrError("var x = y.function;");

    reportEs3Props = true;

    String message =
        "Keywords and reserved words are not allowed as unquoted property " +
        "names in older versions of JavaScript. " +
        "If you are targeting newer versions of JavaScript, " +
        "set the appropriate language_in option.";
    JSError error = assertWarning(
        "var x = y.function;",
        RhinoErrorReporter.INVALID_ES3_PROP_NAME,
        message);

    assertEquals(1, error.getLineNumber());
    assertEquals(10, error.getCharno());
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValue
  public void testValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testConstValue
  public void testConstValue() {
    
    
    testChecks(" function f(CONST) {}",
        "function f(CONST) {" +
        "  $jscomp.typecheck.checkType(CONST, " +
        "      [$jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValueWithInnerFn
  public void testValueWithInnerFn() {
    testChecks(" function f(i) { function g() {} }",
        "function f(i) {" +
        "  function g() {}" +
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNullValue
  public void testNullValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  $jscomp.typecheck.checkType(i, [$jscomp.typecheck.nullChecker]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValues
  public void testValues() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.valueChecker('number')]);" +
        "  $jscomp.typecheck.checkType(j, " +
        "      [$jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testSkipParamOK
  public void testSkipParamOK() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  $jscomp.typecheck.checkType(j, " +
        "      [$jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUnion
  public void testUnion() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  $jscomp.typecheck.checkType(x, [" +
        "      $jscomp.typecheck.valueChecker('number'), " +
        "      $jscomp.typecheck.valueChecker('string')" +
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
        "  return $jscomp.typecheck.checkType('x', " +
        "      [$jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNativeClass
  public void testNativeClass() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  $jscomp.typecheck.checkType(x, " +
        "      [$jscomp.typecheck.externClassChecker('String')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testQualifiedClass
  public void testQualifiedClass() {
    testChecks("var goog = {}; goog.Foo = function() {};" +
        " function f(x) {}",
        "var goog = {}; goog.Foo = function() {};" +
        "goog.Foo.prototype['instance_of__goog.Foo'] = true;" +
        "function f(x) {" +
        "  $jscomp.typecheck.checkType(x, " +
        "    [$jscomp.typecheck.classChecker('goog.Foo')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInnerClasses
  public void testInnerClasses() {
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
        "  $jscomp.typecheck.checkType(i, " +
        "    [$jscomp.typecheck.interfaceChecker('I')])" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterface
  public void testImplementedInterface() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function f(i) {" +
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.interfaceChecker('I')])" +
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
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.interfaceChecker('I')])" +
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
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.interfaceChecker('I')])" +
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
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.interfaceChecker('I')])" +
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

// com.google.javascript.jscomp.SanityCheckTest::testUnnormalizeNodeTypes
  public void testUnnormalizeNodeTypes() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().reportCodeChange();
        root.getFirstChild().addChildToBack(
              new Node(Token.IF, new Node(Token.TRUE), new Node(Token.EMPTY)));
      }
    };

    boolean exceptionCaught = false;
    try {
      test("var x = 3;", "var x=3;0;0");
    } catch (IllegalStateException e) {
      assertEquals("Expected BLOCK but was EMPTY Reference node EMPTY",
          e.getMessage());
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

// com.google.javascript.jscomp.SanityCheckTest::testUnnormalized
  public void testUnnormalized() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().setLifeCycleStage(LifeCycleStage.NORMALIZED);
      }
    };

    boolean exceptionCaught = false;
    try {
      test("while(1){}", "while(1){}");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(
          "Normalize constraints violated:\nWHILE node"));
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

// com.google.javascript.jscomp.SanityCheckTest::testConstantAnnotationMismatch
  public void testConstantAnnotationMismatch() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().reportCodeChange();
        Node name = Node.newString(Token.NAME, "x");
        name.putBooleanProp(Node.IS_CONSTANT_NAME, true);
        root.getFirstChild().addChildToBack(new Node(Token.EXPR_RESULT, name));
        getLastCompiler().setLifeCycleStage(LifeCycleStage.NORMALIZED);
      }
    };

    boolean exceptionCaught = false;
    try {
      test("var x;", "var x; x;");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(
          "The name x is not consistently annotated as constant."));
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
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

// com.google.javascript.jscomp.ScopedAliasesTest::testShadowedScopedVar
  public void testShadowedScopedVar() {
    test("var goog = {};" +
         "goog.bar = {};" +
         "goog.scope(function() {" +
         "  var bar = goog.bar;" +
         
         
         "  bar.newMethod = function(goog) { return goog + bar; };" +
         "});",
         "var goog={};" +
         "goog.bar={};" +
         "goog.bar.newMethod=function(goog$$1){return goog$$1 + goog.bar}");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testShadowedScopedVarTwoScopes
  public void testShadowedScopedVarTwoScopes() {
    test("var goog = {};" +
         "goog.bar = {};" +
         "goog.scope(function() {" +
         "  var bar = goog.bar;" +
         "  bar.newMethod = function(goog, a) { return bar + a; };" +
         "});" +
         "goog.scope(function() {" +
         "  var bar = goog.bar;" +
         "  bar.newMethod2 = function(goog, b) { return bar + b; };" +
         "});",
         "var goog={};" +
         "goog.bar={};" +
         "goog.bar.newMethod=function(goog$$1, a){return goog.bar + a};" +
         "goog.bar.newMethod2=function(goog$$1, b){return goog.bar + b};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testUsingObjectLiteralToEscapeScoping
  public void testUsingObjectLiteralToEscapeScoping() {
    
    
    
    
    
    test(
        "var goog = {};" +
        "goog.bar = {};" +
        "goog.scope(function() {" +
        "  var bar = goog.bar;" +
        "  var baz = goog.bar.baz;" +
        "  goog.foo = function() {" +
        "    goog.bar = {baz: 3};" +
        "    return baz;" +
        "  };" +
        "});",
        "var goog = {};" +
        "goog.bar = {};" +
        "goog.foo = function(){" +
        "  goog.bar = {baz:3};" +
        "  return goog.bar.baz;" +
        "};");
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
        " foo.Foo.actual = function(x) {3};" +
        "var Foo = foo.Foo;" +
        " Foo.Bar = function() {};" +
        " foo.Foo.expected = function(x) {};",

        "\n" +
        "foo.Foo = function() {};" +
        " foo.Foo.actual = function(x) {3};" +
        " foo.Foo.Bar = function() {};" +
        " foo.Foo.expected = function(x) {};");
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

// com.google.javascript.jscomp.ScopedAliasesTest::testIssue772
  public void testIssue772() {
    testTypes(
        "var b = a.b;" +
        "var c = b.c;",
        " types.actual;" +
        " types.expected;");
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

// com.google.javascript.jscomp.ScopedAliasesTest::testCtorAlias
  public void testCtorAlias() {
    test("var x = {y: {}};" +
         "goog.scope(function() {" +
         "  var y = x.y;" +
         "  y.ClassA = function() { this.b = new ClassB(); };" +
         "  y.ClassB = function() {};" +
         "  var ClassB = y.ClassB;" +
         "});",
         "var x = {y: {}};" +
         "x.y.ClassA = function() { this.b = new x.y.ClassB(); };" +
         "x.y.ClassB = function() { };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasCycle
  public void testAliasCycle() {
    test("var x = {y: {}};" +
         "goog.scope(function() {" +
         "  var y = z.x;" +
         "  var z = y.x;" +
         "  y.ClassA = function() {};" +
         "  z.ClassB = function() {};" +
         "});", null,
         ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
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
    testScopedFailure("function f() {}",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testOkAliasLocal
  public void testOkAliasLocal() {
    testScoped("var x = 10;",
               SCOPE_NAMESPACE + "$jscomp.scope.x = 10");
    testScoped("var x = goog['dom'];",
               SCOPE_NAMESPACE + "$jscomp.scope.x = goog['dom']");
    testScoped("var x = 10, y = 9;",
               SCOPE_NAMESPACE + "$jscomp.scope.x = 10; $jscomp.scope.y = 9;");
    testScoped("var x = 10, y = 9; goog.getX = function () { return x + y; }",
               SCOPE_NAMESPACE + "$jscomp.scope.x = 10; $jscomp.scope.y = 9;" +
               "goog.getX = function () { " +
               "    return $jscomp.scope.x + $jscomp.scope.y; }");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasReassign
  public void testAliasReassign() {
    testScopedFailure("var x = 3; x = 5;",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testMultipleLocals
  public void testMultipleLocals() {
    test("goog.scope(function () { var x = 3; });" +
         "goog.scope(function () { var x = 4; });",
         SCOPE_NAMESPACE + "$jscomp.scope.x = 3; $jscomp.scope.x$1 = 4");
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
    verifyAliasTransformationPosition(1, 0, 2, 1, positions.get(0));

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
    verifyAliasTransformationPosition(1, 0, 3, 1, positions.get(0));

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

    verifyAliasTransformationPosition(1, 0, 6, 0, positions.get(0));

    verifyAliasTransformationPosition(8, 0, 11, 4, positions.get(1));

    assertEquals(2, spy.constructedAliases.size());
    AliasSpy aliasSpy = (AliasSpy) spy.constructedAliases.get(0);
    assertEquals("goog", aliasSpy.observedDefinitions.get("g"));

    aliasSpy = (AliasSpy) spy.constructedAliases.get(1);
    assertEquals("namespace.Zoo", aliasSpy.observedDefinitions.get("z"));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testNameCondition
  public void testNameCondition() throws Exception {
    FlowScope blind = newScope();
    Node condition = createVar(blind, "a", createNullableType(STRING_TYPE));

    
    FlowScope informedTrue = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, true);
    assertTypeEquals(STRING_TYPE, getVarType(informedTrue, "a"));

    
    FlowScope informedFalse = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, false);
    assertTypeEquals(createNullableType(STRING_TYPE),
        getVarType(informedFalse, "a"));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testNegatedNameCondition
  public void testNegatedNameCondition() throws Exception {
    FlowScope blind = newScope();
    Node a = createVar(blind, "a", createNullableType(STRING_TYPE));
    Node condition = new Node(Token.NOT);
    condition.addChildToBack(a);

    
    FlowScope informedTrue = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, true);
    assertTypeEquals(createNullableType(STRING_TYPE),
        getVarType(informedTrue, "a"));

    
    FlowScope informedFalse = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, false);
    assertTypeEquals(STRING_TYPE, getVarType(informedFalse, "a"));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testAssignCondition1
  public void testAssignCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.ASSIGN,
        createVar(blind, "a", createNullableType(OBJECT_TYPE)),
        createVar(blind, "b", createNullableType(OBJECT_TYPE)),
        Sets.newHashSet(
            new TypedName("a", OBJECT_TYPE),
            new TypedName("b", OBJECT_TYPE)),
        Sets.newHashSet(
            new TypedName("a", NULL_TYPE),
            new TypedName("b", NULL_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition1
  public void testSheqCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        createNumber(56),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition2
  public void testSheqCondition2() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createNumber(56),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition3
  public void testSheqCondition3() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "b", createUnionType(STRING_TYPE, BOOLEAN_TYPE)),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", STRING_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE)),
            new TypedName("b",
                createUnionType(STRING_TYPE, BOOLEAN_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition4
  public void testSheqCondition4() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition5
  public void testSheqCondition5() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(NULL_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", NULL_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition6
  public void testSheqCondition6() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(NUMBER_TYPE, VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a",
                createUnionType(STRING_TYPE, VOID_TYPE)),
            new TypedName("b",
                createUnionType(NUMBER_TYPE, VOID_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition1
  public void testShneCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        createNumber(56),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition2
  public void testShneCondition2() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createNumber(56),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition3
  public void testShneCondition3() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "b", createUnionType(STRING_TYPE, BOOLEAN_TYPE)),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE)),
            new TypedName("b",
                createUnionType(STRING_TYPE, BOOLEAN_TYPE))),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", STRING_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition4
  public void testShneCondition4() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition5
  public void testShneCondition5() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(NULL_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(NULL_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", NULL_TYPE)),
        Sets.newHashSet(new TypedName("a", NULL_TYPE),
            new TypedName("b", NULL_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition6
  public void testShneCondition6() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(NUMBER_TYPE, VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a",
                createUnionType(STRING_TYPE, VOID_TYPE)),
            new TypedName("b",
                createUnionType(NUMBER_TYPE, VOID_TYPE))),
        Sets.newHashSet(
            new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition1
  public void testEqCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        createVar(blind, "a", createUnionType(BOOLEAN_TYPE, VOID_TYPE)),
        createNull(),
        Sets.newHashSet(new TypedName("a", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", BOOLEAN_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition2
  public void testEqCondition2() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.NE,
        createNull(),
        createVar(blind, "a", createUnionType(BOOLEAN_TYPE, VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", BOOLEAN_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition3
  public void testEqCondition3() throws Exception {
    FlowScope blind = newScope();
    
    JSType nullableOptionalNumber =
        createUnionType(NULL_TYPE, VOID_TYPE, NUMBER_TYPE);
    
    JSType nullUndefined =
        createUnionType(VOID_TYPE, NULL_TYPE);
    testBinop(blind,
        Token.EQ,
        createVar(blind, "a", nullableOptionalNumber),
        createNull(),
        Sets.newHashSet(new TypedName("a", nullUndefined)),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition4
  public void testEqCondition4() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        createVar(blind, "a", VOID_TYPE),
        createVar(blind, "b", VOID_TYPE),
        Sets.newHashSet(
            new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a", NO_TYPE),
            new TypedName("b", NO_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInequalitiesCondition1
  public void testInequalitiesCondition1() {
    for (int op : Arrays.asList(Token.LT, Token.GT, Token.LE, Token.GE)) {
      FlowScope blind = newScope();
      testBinop(blind,
          op,
          createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
          createNumber(8),
          Sets.newHashSet(
              new TypedName("a", STRING_TYPE)),
          Sets.newHashSet(new TypedName("a",
              createUnionType(STRING_TYPE, VOID_TYPE))));
    }
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInequalitiesCondition2
  public void testInequalitiesCondition2() {
    for (int op : Arrays.asList(Token.LT, Token.GT, Token.LE, Token.GE)) {
      FlowScope blind = newScope();
      testBinop(blind,
          op,
          createVar(blind, "a",
              createUnionType(STRING_TYPE, NUMBER_TYPE, VOID_TYPE)),
          createVar(blind, "b",
              createUnionType(NUMBER_TYPE, NULL_TYPE)),
          Sets.newHashSet(
              new TypedName("a",
              createUnionType(STRING_TYPE, NUMBER_TYPE)),
              new TypedName("b",
              createUnionType(NUMBER_TYPE, NULL_TYPE))),
          Sets.newHashSet(
              new TypedName("a",
              createUnionType(STRING_TYPE, NUMBER_TYPE, VOID_TYPE)),
              new TypedName("b",
              createUnionType(NUMBER_TYPE, NULL_TYPE))));
    }
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInequalitiesCondition3
  public void testInequalitiesCondition3() {
    for (int op : Arrays.asList(Token.LT, Token.GT, Token.LE, Token.GE)) {
      FlowScope blind = newScope();
      testBinop(blind,
          op,
          createUntypedNumber(8),
          createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
          Sets.newHashSet(
              new TypedName("a", STRING_TYPE)),
          Sets.newHashSet(new TypedName("a",
              createUnionType(STRING_TYPE, VOID_TYPE))));
    }
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testAnd
  public void testAnd() {
    FlowScope blind = newScope();
    testBinop(blind,
      Token.AND,
      createVar(blind, "b", createUnionType(STRING_TYPE, NULL_TYPE)),
      createVar(blind, "a", createUnionType(NUMBER_TYPE, VOID_TYPE)),
      Sets.newHashSet(new TypedName("a", NUMBER_TYPE),
          new TypedName("b", STRING_TYPE)),
      Sets.newHashSet(new TypedName("a",
          createUnionType(NUMBER_TYPE, VOID_TYPE)),
          new TypedName("b",
          createUnionType(STRING_TYPE, NULL_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testTypeof1
  public void testTypeof1() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        new Node(Token.TYPEOF, createVar(blind, "a", OBJECT_TYPE)),
        Node.newString("function"),
        Sets.newHashSet(
            new TypedName("a", U2U_CONSTRUCTOR_TYPE)),
        Sets.newHashSet(
            new TypedName("a", OBJECT_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testTypeof2
  public void testTypeof2() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        new Node(Token.TYPEOF, createVar(blind, "a", ALL_TYPE)),
        Node.newString("function"),
        Sets.newHashSet(
            new TypedName("a", U2U_CONSTRUCTOR_TYPE)),
        Sets.newHashSet(
            new TypedName("a", ALL_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testTypeof3
  public void testTypeof3() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        new Node(Token.TYPEOF, createVar(
            blind, "a", OBJECT_NUMBER_STRING_BOOLEAN)),
        Node.newString("function"),
        Sets.newHashSet(
            new TypedName("a", U2U_CONSTRUCTOR_TYPE)),
        Sets.newHashSet(
            new TypedName("a", OBJECT_NUMBER_STRING_BOOLEAN)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testTypeof4
  public void testTypeof4() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        new Node(Token.TYPEOF, createVar(
            blind, "a", createUnionType(
                U2U_CONSTRUCTOR_TYPE,NUMBER_STRING_BOOLEAN))),
        Node.newString("function"),
        Sets.newHashSet(
            new TypedName("a", U2U_CONSTRUCTOR_TYPE)),
        Sets.newHashSet(
            new TypedName("a", NUMBER_STRING_BOOLEAN)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf
  public void testInstanceOf() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x", UNKNOWN_TYPE),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf2
  public void testInstanceOf2() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x",
            createUnionType(STRING_OBJECT_TYPE, NUMBER_OBJECT_TYPE)),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("x", NUMBER_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf3
  public void testInstanceOf3() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x", OBJECT_TYPE),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("x", OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf4
  public void testInstanceOf4() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x", ALL_TYPE),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowSimple1
  public void testShadowSimple1() {
    test("function foo(x) { return function (y) {} }",
         "function   b(a) { return function (a) {} }");

    generatePseudoNames = true;

    test("function  foo  ( x  ) { return function ( y  ) {} }",
         "function $foo$$($x$$) { return function ($x$$) {} }");

  }

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowSimple2
  public void testShadowSimple2() {
    test("function foo(x,y) { return function (y,z) {} }",
         "function   c(a,b) { return function (a,b) {} }");

    generatePseudoNames = true;

    test("function  foo  ( x  , y  ) { return function ( y  , z  ) {} }",
         "function $foo$$($x$$,$y$$) { return function ($x$$,$y$$) {} }");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowMostUsedVar
  public void testShadowMostUsedVar() {
    generatePseudoNames = true;
    test("function  foo  () {var  x  ; var  y  ;  y  ; y  ; y  ; x  ;" +
         "  return function ( k  ) {} }",

         "function $foo$$() {var $x$$; var $y$$; $y$$;$y$$;$y$$;$x$$;" +
         "  return function ($y$$) {} }");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testNoShadowReferencedVariables
  public void testNoShadowReferencedVariables() {
    generatePseudoNames = true;
    test("function  f1  () { var  x  ; x  ; x  ; x  ;" +
         "  return function  f2  ( y  ) {" +
         "    return function  f3  () { x  } }}",
         "function $f1$$() { var $x$$;$x$$;$x$$;$x$$;" +
         "  return function $f2$$($y$$) {" +
         "    return function $f3$$() {$x$$} }}");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testNoShadowGlobalVariables
  public void testNoShadowGlobalVariables() {
    generatePseudoNames = true;
    test("var  x  ;  x  ; function  foo  () { return function ( y  ) {}}",
         "var $x$$; $x$$; function $foo$$() { return function ($y$$) {}}");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowBleedInFunctionName
  public void testShadowBleedInFunctionName() {
    generatePseudoNames = true;
    test("function  foo  () { function  b  ( y  ) { y  }  b  ;  b  ;}",
         "function $foo$$() { function $b$$($b$$) {$b$$} $b$$; $b$$;}");
   }

// com.google.javascript.jscomp.ShadowVariables2Test::testNoShadowLessPopularName
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

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowFunctionName
  public void testShadowFunctionName() {
    generatePseudoNames = true;
    test("var  g   = function() {" +
         "  var  x  ; return function(){function  y  (){}}}",
         "var $g$$ = function() {" +
         "  var $x$$; return function(){function $x$$(){}}}");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowLotsOfScopes1
  public void testShadowLotsOfScopes1() {
    generatePseudoNames = true;
    test("var  g   = function( x  ) { return function() { return function() {" +
         " return function() { var  y   }}}}",
         "var $g$$ = function($x$$) { return function() { return function() {" +
         " return function() { var $x$$ }}}}");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowLotsOfScopes2
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

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowLotsOfScopes3
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

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowLotsOfScopes4
  public void testShadowLotsOfScopes4() {
    
    test("var g = function(x) { return function() { return function() {" +
         " return function(){return function(){};var m};var n};var o}}",
         "var b = function(a) { return function() { return function() {" +
         " return function(){return function(){};var a};var a};var a}}");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowLotsOfScopes5
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

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowWithShadowAlready
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

// com.google.javascript.jscomp.ShadowVariables2Test::testShadowBug1
  public void testShadowBug1() {
    generatePseudoNames = true;
    test("function  f  ( x  ) { return function( y  ) {" +
         "    return function( x  ) {  x   +  y  ; }}}",
         "function $f$$($x$$) { return function($y$$) {" +
         "    return function($x$$) { $x$$ + $y$$; }}}");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testOptimal
  public void testOptimal() {
    
    test("function f(x) { function g(y) { function h(x) {}}}",
         "function c(a) { function b(a) { function b(a) {}}}");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testSharingAcrossInnerScopes
  public void testSharingAcrossInnerScopes() {
    test("function f() {var f=function g(){g()}; var x=function y(){y()}}",
         "function c() {var d=function a(){a()}; var e=function b(){b()}}");
    test("function f(x) { return x ? function(y){} : function(z) {} }",
         "function b(a) { return a ? function(a){} : function(a) {} }");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testExportedLocal1
  public void testExportedLocal1() {
    test("function f(a) { a();a();a(); return function($super){} }",
         "function b(a) { a();a();a(); return function($super){} }");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testExportedLocal2
  public void testExportedLocal2() {
    test("function f($super) { $super();$super(); return function(a){} }",
         "function a($super) { $super();$super(); return function(b){} }");
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testRenameMapHasNoDuplicates
  public void testRenameMapHasNoDuplicates() {
    test("function foo(x) { return function (y) {} }",
         "function   b(a) { return function (a) {} }");

    VariableMap vm = pass.getVariableMap();
    try {
      vm.getNewNameToOriginalNameMap();
    } catch (java.lang.IllegalArgumentException unexpected) {
      fail("Invalid VariableMap generated: " +
           vm.getOriginalNameToNewNameMap().toString());
    }
  }

// com.google.javascript.jscomp.ShadowVariables2Test::testBug4172539
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

// com.google.javascript.jscomp.ShadowVariablesTest::testRenameMapHasNoDuplicates
  public void testRenameMapHasNoDuplicates() {
    test("function foo(x) { return function (y) {} }",
         "function   b(a) { return function (a) {} }");

    VariableMap vm = pass.getVariableMap();
    try {
      vm.getNewNameToOriginalNameMap();
    } catch (java.lang.IllegalArgumentException unexpected) {
      fail("Invalid VariableMap generated: " +
           vm.getOriginalNameToNewNameMap().toString());
    }
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
        ImmutableSet.of("DEF STRING_KEY null -> NUMBER",
                        "USE GETPROP o.a -> [NUMBER]"));

    
    checkDefinitionsInJs(
      "({'a' : 1}); o['a']",
      ImmutableSet.<String>of("DEF STRING_KEY null -> NUMBER"));

    checkDefinitionsInJs(
      "({1 : 1}); o[1]",
      ImmutableSet.<String>of("DEF STRING_KEY null -> NUMBER"));

    checkDefinitionsInJs(
        "var a = {b : 1}; a.b",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "DEF STRING_KEY null -> NUMBER",
                        "USE NAME a -> [<null>]",
                        "USE GETPROP a.b -> [NUMBER]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineGet
  public void testDefineGet() throws Exception {
    
    checkDefinitionsInJs(
      "({get a() {}}); o.a",
      ImmutableSet.of("DEF GETTER_DEF null -> FUNCTION",
                      "USE GETPROP o.a -> [FUNCTION]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineSet
  public void testDefineSet() throws Exception {
    
    checkDefinitionsInJs(
      "({set a(b) {}}); o.a",
      ImmutableSet.of("DEF NAME b -> <null>",
                      "DEF SETTER_DEF null -> FUNCTION",
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
            "DEF STRING_KEY null -> EXTERN NUMBER",
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

// com.google.javascript.jscomp.StatementFusionTest::testFuseIntoVanillaFor
  public void testFuseIntoVanillaFor() {
    fuse("a;b;c;for(;g;){}", "for(a,b,c;g;){}");
    fuse("a;b;c;for(d;g;){}", "for(a,b,c,d;g;){}");
    fuse("a;b;c;for(d,e;g;){}", "for(a,b,c,d,e;g;){}");
    fuseSame("a();for(var x;g;){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFuseIntoLabel
  public void testFuseIntoLabel() {
    fuse("a;b;c;label:for(x in y){}", "label:for(x in a,b,c,y){}");
    fuse("a;b;c;label:for(;g;){}", "label:for(a,b,c;g;){}");
    fuse("a;b;c;l1:l2:l3:for(;g;){}", "l1:l2:l3:for(a,b,c;g;){}");
    fuseSame("a;b;c;label:while(true){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFuseIntoBlock
  public void testFuseIntoBlock() {
    fuse("a;b;c;{d;e;f}", "{a,b,c,d,e,f}");
    fuse("a;b; label: { if(q) break label; bar(); }",
         "label: { if(a,b,q) break label; bar(); }");
    fuseSame("a;b;c;{var x;d;e;}");
    fuseSame("a;b;c;label:{break label;d;e;}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testNoFuseIntoWhile
  public void testNoFuseIntoWhile() {
    fuseSame("a;b;c;while(x){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testNoFuseIntoDo
  public void testNoFuseIntoDo() {
    fuseSame("a;b;c;do{}while(x)");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFavorComma1
  public void testFavorComma1() {
    favorsCommas = true;
    test("a;b;c", "a,b,c");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFavorComma2
  public void testFavorComma2() {
    favorsCommas = true;
    test("a;b;c;if(d){}", "if(a,b,c,d){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFavorComma3
  public void testFavorComma3() {
    favorsCommas = true;
    test("a;b;c;if(d){} d;e;f", "if(a,b,c,d){}d,e,f");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFavorComma4
  public void testFavorComma4() {
    favorsCommas = true;
    test("if(d){} d;e;f", "if(d){}d,e,f");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFavorComma5
  public void testFavorComma5() {
    favorsCommas = true;
    test("a;b;c;if(d){}d;e;f;if(g){}", "if(a,b,c,d){}if(d,e,f,g){}");
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

    testSame(
        "'use strict';\n" +
        "function App() {}\n" +
        "App.prototype = {\n" +
        "  get appData() { return this.appData_; },\n" +
        "  set appData(data) { this.appData_ = data; }\n" +
        "};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testFunctionDecl
  public void testFunctionDecl() {
    testSame("function g() {}");
    testSame("var g = function() {};");
    testSame("(function() {})();");
    testSame("(function() {});");
    testSame(inFn("function g() {}"));
    testSame(inFn("var g = function() {};"));
    testSame(inFn("(function() {})();"));
    testSame(inFn("(function() {});"));

    test("{function g() {}}", null, StrictModeCheck.BAD_FUNCTION_DECLARATION);
    testSame("{var g = function () {}}");
    testSame("{(function g() {})()}");

    test("var x;if (x) { function g(){} }", null,
        StrictModeCheck.BAD_FUNCTION_DECLARATION);
    testSame("var x;if (x) {var g = function () {}}");
    testSame("var x;if (x) {(function g() {})()}");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testFunctionDecl2
  public void testFunctionDecl2() {
    test("{function g() {}}", null, StrictModeCheck.BAD_FUNCTION_DECLARATION);
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
    
    
    test("goog.debug.Logger = 1; goog.debug.Logger.prop=2; ", "");
    test("this.blcLogger_.level=x", "");
    test("goog.ui.Component.logger.prop=y", "");
    test("goog.ui.Component.logger.prop.foo.bar=baz", "");
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

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames1
  public void testStripCallsToStrippedNames1() {
    test("a = function() { this.logger_ = function(msg){}; };" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
    test("a = function() {};" +
         "a.prototype.logger_ = function(msg) {};" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames2
  public void testStripCallsToStrippedNames2() {
    test("a = function() {};" +
         "a.prototype.logger_ = function(msg) {};" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames3
  public void testStripCallsToStrippedNames3() {
    test("a = function() { this.logger_ = function(msg){}; };" +
         "a.prototype.b = function() { this.logger_('hi').foo = 2; }",
         "a=function(){};a.prototype.b=function(){2;}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames4
  public void testStripCallsToStrippedNames4() {
    test("a = this.logger_().foo;",
         "a = null;");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripVarsInitializedFromStrippedNames1
  public void testStripVarsInitializedFromStrippedNames1() {
    test("a = function() { this.logger_ = function() { return 1; }; };" +
         "a.prototype.b = function() { " +
         "  var one = this.logger_(); if (one) foo() }",
          "a=function(){};a.prototype.b=function(){if(null)foo()}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripVarsInitializedFromStrippedNames2
  public void testStripVarsInitializedFromStrippedNames2() {
    test("a = function() { this.logger_ = function() { return 1; }; };" +
         "a.prototype.b = function() { " +
         "  var one = this.logger_.foo.bar(); if (one) foo() }",
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

// com.google.javascript.jscomp.StripCodeTest::testNewOperatior3
  public void testNewOperatior3() {
    test("(new goog.debug.Logger()).foo().bar = 2;",
         "2;");
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
