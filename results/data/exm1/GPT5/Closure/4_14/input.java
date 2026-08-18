// buggy code
  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> enclosing) {
    // TODO(user): Investigate whether it is really necessary to keep two
    // different mechanisms for resolving named types, and if so, which order
    // makes more sense. Now, resolution via registry is first in order to
    // avoid triggering the warnings built into the resolution via properties.
    boolean resolved = resolveViaRegistry(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
    }

    if (resolved) {
      super.resolveInternal(t, enclosing);
      finishPropertyContinuations();
      return registry.isLastGeneration() ?
          getReferencedType() : this;
    }

    resolveViaProperties(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
    }

    super.resolveInternal(t, enclosing);
    if (isResolved()) {
      finishPropertyContinuations();
    }
    return registry.isLastGeneration() ?
        getReferencedType() : this;
  }

// relevant test
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
    testScopedFailure("function f() {}",
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

    testSame(
        "'use strict';\n" +
        "function App() {}\n" +
        "App.prototype = {\n" +
        "  get appData() { return this.appData_; },\n" +
        "  set appData(data) { this.appData_ = data; }\n" +
        "};");
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

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVar
  public void testGlobalVar() throws Exception {
    SymbolTable table = createSymbolTable(
        " var x = 5;");
    assertNull(getGlobalVar(table, "y"));
    assertNotNull(getGlobalVar(table, "x"));
    assertEquals("number", getGlobalVar(table, "x").getType().toString());

    
    assertEquals(2, getVars(table).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences
  public void testGlobalThisReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var x = this; function f() { return this + this + this; }");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(1, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences2
  public void testGlobalThisReferences2() throws Exception {
    
    SymbolTable table = createSymbolTable("");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(0, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences3
  public void testGlobalThisReferences3() throws Exception {
    SymbolTable table = createSymbolTable("this.foo = {}; this.foo.bar = {};");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisPropertyReferences
  public void testGlobalThisPropertyReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function Foo() {} this.Foo;");

    Symbol foo = getGlobalVar(table, "Foo");
    assertNotNull(foo);

    List<Reference> refs = table.getReferenceList(foo);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVarReferences
  public void testGlobalVarReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var x = 5; x = 6;");
    Symbol x = getGlobalVar(table, "x");
    List<Reference> refs = table.getReferenceList(x);

    assertEquals(2, refs.size());
    assertEquals(x.getDeclaration(), refs.get(0));
    assertEquals(Token.VAR, refs.get(0).getNode().getParent().getType());
    assertEquals(Token.ASSIGN, refs.get(1).getNode().getParent().getType());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalVarReferences
  public void testLocalVarReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "function f(x) { return x; }");
    Symbol x = getLocalVar(table, "x");
    List<Reference> refs = table.getReferenceList(x);

    assertEquals(2, refs.size());
    assertEquals(x.getDeclaration(), refs.get(0));
    assertEquals(Token.PARAM_LIST, refs.get(0).getNode().getParent().getType());
    assertEquals(Token.RETURN, refs.get(1).getNode().getParent().getType());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences
  public void testLocalThisReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() { this.foo = 3; this.bar = 5; }");

    Symbol f = getGlobalVar(table, "F");
    assertNotNull(f);

    Symbol t = table.getParameterInFunction(f, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences2
  public void testLocalThisReferences2() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() {}" +
        "F.prototype.baz = " +
        "    function() { this.foo = 3; this.bar = 5; };");

    Symbol baz = getGlobalVar(table, "F.prototype.baz");
    assertNotNull(baz);

    Symbol t = table.getParameterInFunction(baz, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences3
  public void testLocalThisReferences3() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() {}");

    Symbol baz = getGlobalVar(table, "F");
    assertNotNull(baz);

    Symbol t = table.getParameterInFunction(baz, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(0, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testNamespacedReferences
  public void testNamespacedReferences() throws Exception {
    
    
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.dom = {};" +
        "goog.dom.DomHelper = function(){};");
    Symbol goog = getGlobalVar(table, "goog");
    assertNotNull(goog);
    assertEquals(3, Iterables.size(table.getReferences(goog)));

    Symbol googDom = getGlobalVar(table, "goog.dom");
    assertNotNull(googDom);
    assertEquals(2, Iterables.size(table.getReferences(googDom)));

    Symbol googDomHelper = getGlobalVar(table, "goog.dom.DomHelper");
    assertNotNull(googDomHelper);
    assertEquals(1, Iterables.size(table.getReferences(googDomHelper)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testIncompleteNamespacedReferences
  public void testIncompleteNamespacedReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n" +
        "goog.dom.DomHelper = function(){};\n" +
        "var y = goog.dom.DomHelper;\n");
    Symbol goog = getGlobalVar(table, "goog");
    assertNotNull(goog);
    assertEquals(2, table.getReferenceList(goog).size());

    Symbol googDom = getGlobalVar(table, "goog.dom");
    assertNotNull(googDom);
    assertEquals(2, table.getReferenceList(googDom).size());

    Symbol googDomHelper = getGlobalVar(table, "goog.dom.DomHelper");
    assertNotNull(googDomHelper);
    assertEquals(2, Iterables.size(table.getReferences(googDomHelper)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalRichObjectReference
  public void testGlobalRichObjectReference() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n" +
        "function A(){};\n" +
        " A.prototype.b;\n" +
        " var a = new A();\n" +
        "function g() {\n" +
        "  return a.b ? 'x' : 'y';\n" +
        "}\n" +
        "(function() {\n" +
        "  var x; if (x) { x = a.b.b; } else { x = a.b.c; }\n" +
        "  return x;\n" +
        "})();\n");

    Symbol ab = getGlobalVar(table, "a.b");
    assertNull(ab);

    Symbol propB = getGlobalVar(table, "A.prototype.b");
    assertNotNull(propB);
    assertEquals(5, table.getReferenceList(propB).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testRemovalOfNamespacedReferencesOfProperties
  public void testRemovalOfNamespacedReferencesOfProperties()
      throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        " DomHelper.method = function() {};");

    Symbol domHelper = getGlobalVar(table, "DomHelper");
    assertNotNull(domHelper);

    Symbol domHelperNamespacedMethod = getGlobalVar(table, "DomHelper.method");
    assertEquals("method", domHelperNamespacedMethod.getName());

    Symbol domHelperMethod = domHelper.getPropertyScope().getSlot("method");
    assertNotNull(domHelperMethod);
  }

// com.google.javascript.jscomp.SymbolTableTest::testGoogScopeReferences
  public void testGoogScopeReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.scope = function() {};" +
        "goog.scope(function() {});");
    Symbol googScope = getGlobalVar(table, "goog.scope");
    assertNotNull(googScope);
    assertEquals(2, Iterables.size(table.getReferences(googScope)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGoogRequireReferences
  public void testGoogRequireReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.provide = function() {};" +
        "goog.require = function() {};" +
        "goog.provide('goog.dom');" +
        "goog.require('goog.dom');");
    Symbol goog = getGlobalVar(table, "goog");
    assertNotNull(goog);

    
    
    
    
    
    
    
    
    assertEquals(8, Iterables.size(table.getReferences(goog)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGoogRequireReferences2
  public void testGoogRequireReferences2() throws Exception {
    options.brokenClosureRequiresLevel = CheckLevel.OFF;
    SymbolTable table = createSymbolTable(
        "foo.bar = function(){};  
        + "goog.require('foo.bar')\n");
    Symbol fooBar = getGlobalVar(table, "foo.bar");
    assertNotNull(fooBar);
    assertEquals(2, Iterables.size(table.getReferences(fooBar)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVarInExterns
  public void testGlobalVarInExterns() throws Exception {
    SymbolTable table = createSymbolTable("customExternFn(1);");
    Symbol fn = getGlobalVar(table, "customExternFn");
    List<Reference> refs = table.getReferenceList(fn);
    assertEquals(2, refs.size());

    SymbolScope scope = table.getEnclosingScope(refs.get(0).getNode());
    assertTrue(scope.isGlobalScope());
    assertEquals(SymbolTable.GLOBAL_THIS,
        table.getSymbolForScope(scope).getName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalVarInExterns
  public void testLocalVarInExterns() throws Exception {
    SymbolTable table = createSymbolTable("");
    Symbol arg = getLocalVar(table, "customExternArg");
    List<Reference> refs = table.getReferenceList(arg);
    assertEquals(1, refs.size());

    Symbol fn = getGlobalVar(table, "customExternFn");
    SymbolScope scope = table.getEnclosingScope(refs.get(0).getNode());
    assertFalse(scope.isGlobalScope());
    assertEquals(fn, table.getSymbolForScope(scope));
  }

// com.google.javascript.jscomp.SymbolTableTest::testSymbolsForType
  public void testSymbolsForType() throws Exception {
    SymbolTable table = createSymbolTable(
        "function random() { return 1; }" +
        " function Foo() {}" +
        " function Bar() {}" +
        "var x = random() ? new Foo() : new Bar();");

    Symbol x = getGlobalVar(table, "x");
    Symbol foo = getGlobalVar(table, "Foo");
    Symbol bar = getGlobalVar(table, "Bar");
    Symbol fooPrototype = getGlobalVar(table, "Foo.prototype");
    Symbol fn = getGlobalVar(table, "Function");
    Symbol obj = getGlobalVar(table, "Object");
    assertEquals(
        Lists.newArrayList(foo, bar), table.getAllSymbolsForTypeOf(x));
    assertEquals(
        Lists.newArrayList(fn), table.getAllSymbolsForTypeOf(foo));
    assertEquals(
        Lists.newArrayList(foo), table.getAllSymbolsForTypeOf(fooPrototype));
    assertEquals(
        foo,
        table.getSymbolDeclaredBy(
            foo.getType().toMaybeFunctionType()));
  }

// com.google.javascript.jscomp.SymbolTableTest::testStaticMethodReferences
  public void testStaticMethodReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        " DomHelper.method = function() {};" +
        "function f() { var x = DomHelper; x.method() + x.method(); }");

    Symbol method =
        getGlobalVar(table, "DomHelper").getPropertyScope().getSlot("method");
    assertEquals(
        3, Iterables.size(table.getReferences(method)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodReferences
  public void testMethodReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        " DomHelper.prototype.method = function() {};" +
        "function f() { " +
        "  (new DomHelper()).method(); (new DomHelper()).method(); };");

    Symbol method =
        getGlobalVar(table, "DomHelper.prototype.method");
    assertEquals(
        3, Iterables.size(table.getReferences(method)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testSuperClassMethodReferences
  public void testSuperClassMethodReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.inherits = function(a, b) {};" +
        " var A = function(){};" +
        " A.prototype.method = function() {};" +
        "\n" +
        "var B = function(){};\n" +
        "goog.inherits(B, A);" +
        " B.prototype.method = function() {" +
        "  B.superClass_.method();" +
        "};");

    Symbol methodA =
        getGlobalVar(table, "A.prototype.method");
    assertEquals(
        2, Iterables.size(table.getReferences(methodA)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodReferencesMissingTypeInfo
  public void testMethodReferencesMissingTypeInfo() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};\n" +
        " DomHelper.prototype.method = function() {\n" +
        "  this.method();\n" +
        "};\n" +
        "function f() { " +
        "  (new DomHelper()).method();\n" +
        "};");

    Symbol method =
        getGlobalVar(table, "DomHelper.prototype.method");
    assertEquals(
        3, Iterables.size(table.getReferences(method)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testFieldReferencesMissingTypeInfo
  public void testFieldReferencesMissingTypeInfo() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){ this.prop = 1; };\n" +
        " DomHelper.prototype.prop = 2;\n" +
        "function f() {\n" +
        "  return (new DomHelper()).prop;\n" +
        "};");

    Symbol prop =
        getGlobalVar(table, "DomHelper.prototype.prop");
    assertEquals(3, table.getReferenceList(prop).size());

    assertNull(getLocalVar(table, "this.prop"));
  }

// com.google.javascript.jscomp.SymbolTableTest::testFieldReferences
  public void testFieldReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){" +
        "   this.field = 3;" +
        "};" +
        "function f() { " +
        "  return (new DomHelper()).field + (new DomHelper()).field; };");

    Symbol field = getGlobalVar(table, "DomHelper.prototype.field");
    assertEquals(
        3, Iterables.size(table.getReferences(field)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testUndeclaredFieldReferences
  public void testUndeclaredFieldReferences() throws Exception {
    
    
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        "DomHelper.prototype.method = function() { " +
        "  this.field = 3;" +
        "  return x.field;" +
        "}");

    Symbol field = getGlobalVar(table, "DomHelper.prototype.field");
    assertNull(field);
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences
  public void testPrototypeReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function DomHelper() {}" +
        "DomHelper.prototype.method = function() {};");
    Symbol prototype =
        getGlobalVar(table, "DomHelper.prototype");
    assertNotNull(prototype);

    List<Reference> refs = table.getReferenceList(prototype);

    
    assertEquals(refs.toString(), 2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences2
  public void testPrototypeReferences2() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n"
        + "function Snork() {}\n"
        + "Snork.prototype.baz = 3;\n");
    Symbol prototype =
        getGlobalVar(table, "Snork.prototype");
    assertNotNull(prototype);

    List<Reference> refs = table.getReferenceList(prototype);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences3
  public void testPrototypeReferences3() throws Exception {
    SymbolTable table = createSymbolTable(
        " function Foo() {}");
    Symbol fooPrototype = getGlobalVar(table, "Foo.prototype");
    assertNotNull(fooPrototype);

    List<Reference> refs = table.getReferenceList(fooPrototype);
    assertEquals(1, refs.size());
    assertEquals(Token.NAME, refs.get(0).getNode().getType());

    
    
    assertEquals(
        refs.get(0).getNode(),
        table.getReferenceList(getGlobalVar(table, "Foo")).get(0).getNode());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences4
  public void testPrototypeReferences4() throws Exception {
    SymbolTable table = createSymbolTable(
        " function Foo() {}" +
        "Foo.prototype = {bar: 3}");
    Symbol fooPrototype = getGlobalVar(table, "Foo.prototype");
    assertNotNull(fooPrototype);

    List<Reference> refs = Lists.newArrayList(
        table.getReferences(fooPrototype));
    assertEquals(1, refs.size());
    assertEquals(Token.GETPROP, refs.get(0).getNode().getType());
    assertEquals("Foo.prototype", refs.get(0).getNode().getQualifiedName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences5
  public void testPrototypeReferences5() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};  goog.Foo = function() {};");
    Symbol fooPrototype = getGlobalVar(table, "goog.Foo.prototype");
    assertNotNull(fooPrototype);

    List<Reference> refs = table.getReferenceList(fooPrototype);
    assertEquals(1, refs.size());
    assertEquals(Token.GETPROP, refs.get(0).getNode().getType());

    
    
    assertEquals(
        refs.get(0).getNode(),
        table.getReferenceList(
            getGlobalVar(table, "goog.Foo")).get(0).getNode());
  }

// com.google.javascript.jscomp.SymbolTableTest::testReferencesInJSDocType
  public void testReferencesInJSDocType() {
    SymbolTable table = createSymbolTable(
        " function Foo() {}\n" +
        " var x;\n" +
        " function f(x) {}\n" +
        " function g() {}\n" +
        " function Sub() {}");
    Symbol foo = getGlobalVar(table, "Foo");
    assertNotNull(foo);

    List<Reference> refs = table.getReferenceList(foo);
    assertEquals(5, refs.size());

    assertEquals(1, refs.get(0).getNode().getLineno());
    assertEquals(29, refs.get(0).getNode().getCharno());
    assertEquals(3, refs.get(0).getNode().getLength());

    assertEquals(2, refs.get(1).getNode().getLineno());
    assertEquals(11, refs.get(1).getNode().getCharno());

    assertEquals(3, refs.get(2).getNode().getLineno());
    assertEquals(12, refs.get(2).getNode().getCharno());

    assertEquals(4, refs.get(3).getNode().getLineno());
    assertEquals(25, refs.get(3).getNode().getCharno());

    assertEquals(7, refs.get(4).getNode().getLineno());
    assertEquals(13, refs.get(4).getNode().getCharno());
  }

// com.google.javascript.jscomp.SymbolTableTest::testReferencesInJSDocType2
  public void testReferencesInJSDocType2() {
    SymbolTable table = createSymbolTable(
        " function f(x) {}\n");
    Symbol str = getGlobalVar(table, "String");
    assertNotNull(str);

    List<Reference> refs = table.getReferenceList(str);

    
    
    
    
    assertTrue(refs.size() > 1);

    int last = refs.size() - 1;
    for (int i = 0; i < refs.size(); i++) {
      Reference ref = refs.get(i);
      assertEquals(i != last, ref.getNode().isFromExterns());
      if (!ref.getNode().isFromExterns()) {
        assertEquals("in1", ref.getNode().getSourceFileName());
      }
    }
  }

// com.google.javascript.jscomp.SymbolTableTest::testReferencesInJSDocName
  public void testReferencesInJSDocName() {
    String code = " function f(x) {}\n";
    SymbolTable table = createSymbolTable(code);
    Symbol x = getLocalVar(table, "x");
    assertNotNull(x);

    List<Reference> refs = table.getReferenceList(x);
    assertEquals(2, refs.size());

    assertEquals(code.indexOf("x) {"), refs.get(0).getNode().getCharno());
    assertEquals(code.indexOf("x */"), refs.get(1).getNode().getCharno());
    assertEquals("in1",
        refs.get(0).getNode().getSourceFileName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalQualifiedNamesInLocalScopes
  public void testLocalQualifiedNamesInLocalScopes() {
    SymbolTable table = createSymbolTable(
        "function f() { var x = {}; x.number = 3; }");
    Symbol xNumber = getLocalVar(table, "x.number");
    assertNotNull(xNumber);
    assertFalse(table.getScope(xNumber).isGlobalScope());

    assertEquals("number", xNumber.getType().toString());
  }

// com.google.javascript.jscomp.SymbolTableTest::testNaturalSymbolOrdering
  public void testNaturalSymbolOrdering() {
    SymbolTable table = createSymbolTable(
        " var a = {};" +
        " a.b = {};" +
        " function f(x) {}");
    Symbol a = getGlobalVar(table, "a");
    Symbol ab = getGlobalVar(table, "a.b");
    Symbol f = getGlobalVar(table, "f");
    Symbol x = getLocalVar(table, "x");
    Ordering<Symbol> ordering = table.getNaturalSymbolOrdering();
    assertSymmetricOrdering(ordering, a, ab);
    assertSymmetricOrdering(ordering, a, f);
    assertSymmetricOrdering(ordering, f, ab);
    assertSymmetricOrdering(ordering, f, x);
  }

// com.google.javascript.jscomp.SymbolTableTest::testDeclarationDisagreement
  public void testDeclarationDisagreement() {
    SymbolTable table = createSymbolTable(
        " var goog = goog || {};\n" +
        "\n" +
        "goog.addSingletonGetter2 = function(x) {};\n" +
        "\n" +
        "goog.addSingletonGetter = goog.addSingletonGetter2;\n" +
        "\n" +
        "goog.addSingletonGetter = function(x) {};\n");

    Symbol method = getGlobalVar(table, "goog.addSingletonGetter");
    List<Reference> refs = table.getReferenceList(method);
    assertEquals(2, refs.size());

    
    assertEquals(7, method.getDeclaration().getNode().getLineno());
    assertEquals(5, refs.get(1).getNode().getLineno());
  }

// com.google.javascript.jscomp.SymbolTableTest::testMultipleExtends
  public void testMultipleExtends() {
    SymbolTable table = createSymbolTable(
        " var goog = goog || {};\n" +
        "goog.inherits = function(x, y) {};\n" +
        "\n" +
        "goog.A = function() { this.fieldA = this.constructor; };\n" +
        " goog.A.FooA = function() {};\n" +
        " goog.A.prototype.methodA = function() {};\n" +
        "\n" +
        "goog.B = function() { this.fieldB = this.constructor; };\n" +
        "goog.inherits(goog.B, goog.A);\n" +
        " goog.B.prototype.methodB = function() {};\n" +
        "\n" +
        "goog.B2 = function() { this.fieldB = this.constructor; };\n" +
        "goog.inherits(goog.B2, goog.A);\n" +
        " goog.B2.FooB = function() {};\n" +
        " goog.B2.prototype.methodB = function() {};\n" +
        "\n" +
        "goog.C = function() { this.fieldC = this.constructor; };\n" +
        "goog.inherits(goog.C, goog.B);\n" +
        " goog.C.FooC = function() {};\n" +
        " goog.C.prototype.methodC = function() {};\n");

    Symbol bCtor = getGlobalVar(table, "goog.B.prototype.constructor");
    assertNotNull(bCtor);

    List<Reference> bRefs = table.getReferenceList(bCtor);
    assertEquals(2, bRefs.size());
    assertEquals(11, bCtor.getDeclaration().getNode().getLineno());

    Symbol cCtor = getGlobalVar(table, "goog.C.prototype.constructor");
    assertNotNull(cCtor);

    List<Reference> cRefs = table.getReferenceList(cCtor);
    assertEquals(2, cRefs.size());
    assertEquals(26, cCtor.getDeclaration().getNode().getLineno());
  }

// com.google.javascript.jscomp.SymbolTableTest::testJSDocAssociationWithBadNamespace
  public void testJSDocAssociationWithBadNamespace() {
    SymbolTable table = createSymbolTable(
        
        
        
        " goog.Foo = function(){};");

    Symbol foo = getGlobalVar(table, "goog.Foo");
    assertNotNull(foo);

    JSDocInfo info = foo.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isConstructor());
  }

// com.google.javascript.jscomp.SymbolTableTest::testMissingConstructorTag
  public void testMissingConstructorTag() {
    SymbolTable table = createSymbolTable(
        "function F() {" +
        "  this.field1 = 3;" +
        "}" +
        "F.prototype.method1 = function() {" +
        "  this.field1 = 5;" +
        "};" +
        "(new F()).method1();");

    
    
    assertNull(getGlobalVar(table, "F.prototype.field1"));

    Symbol sym = getGlobalVar(table, "F.prototype.method1");
    assertEquals(1, table.getReferenceList(sym).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testTypeCheckingOff
  public void testTypeCheckingOff() {
    options = new CompilerOptions();

    
    SymbolTable table = createSymbolTable(
        "" +
        "function F() {" +
        "  this.field1 = 3;" +
        "}" +
        "F.prototype.method1 = function() {" +
        "  this.field1 = 5;" +
        "};" +
        "(new F()).method1();");
    assertNull(getGlobalVar(table, "F.prototype.field1"));
    assertNull(getGlobalVar(table, "F.prototype.method1"));

    Symbol sym = getGlobalVar(table, "F");
    assertEquals(3, table.getReferenceList(sym).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testSuperClassReference
  public void testSuperClassReference() throws Exception {
    SymbolTable table = createSymbolTable(
        "  var a = {b: {}};\n"
        + "\n"
        + "a.b.BaseClass = function() {};\n"
        + "a.b.BaseClass.prototype.doSomething = function() {\n"
        + "  alert('hi');\n"
        + "};\n"
        + "\n"
        + "a.b.DerivedClass = function() {};\n"
        + "goog.inherits(a.b.DerivedClass, a.b.BaseClass);\n"
        + "\n"
        + "a.b.DerivedClass.prototype.doSomething = function() {\n"
        + "  a.b.DerivedClass.superClass_.doSomething();\n"
        + "};\n");

    Symbol bad = getGlobalVar(
        table, "a.b.DerivedClass.superClass_.doSomething");
    assertNull(bad);

    Symbol good = getGlobalVar(
        table, "a.b.BaseClass.prototype.doSomething");
    assertNotNull(good);

    List<Reference> refs = table.getReferenceList(good);
    assertEquals(2, refs.size());
    assertEquals("a.b.DerivedClass.superClass_.doSomething",
        refs.get(1).getNode().getQualifiedName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testInnerEnum
  public void testInnerEnum() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {}; goog.ui = {};"
        + "  \n"
        + "goog.ui.Zippy = function() {};\n"
        + "\n"
        + "goog.ui.Zippy.EventType = { TOGGLE: 'toggle' };\n");

    Symbol eventType = getGlobalVar(table, "goog.ui.Zippy.EventType");
    assertNotNull(eventType);
    assertTrue(eventType.getType().isEnumType());

    Symbol toggle = getGlobalVar(table, "goog.ui.Zippy.EventType.TOGGLE");
    assertNotNull(toggle);
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodInAnonObject1
  public void testMethodInAnonObject1() throws Exception {
    SymbolTable table = createSymbolTable(
        "var a = {}; a.b = {}; a.b.c = function() {};");
    Symbol a = getGlobalVar(table, "a");
    Symbol ab = getGlobalVar(table, "a.b");
    Symbol abc = getGlobalVar(table, "a.b.c");

    assertNotNull(abc);
    assertEquals(1, table.getReferenceList(abc).size());

    assertEquals("{b: {c: function (): undefined}}", a.getType().toString());
    assertEquals("{c: function (): undefined}", ab.getType().toString());
    assertEquals("function (): undefined", abc.getType().toString());
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodInAnonObject2
  public void testMethodInAnonObject2() throws Exception {
    SymbolTable table = createSymbolTable(
        "var a = {b: {c: function() {}}};");
    Symbol a = getGlobalVar(table, "a");
    Symbol ab = getGlobalVar(table, "a.b");
    Symbol abc = getGlobalVar(table, "a.b.c");

    assertNotNull(abc);
    assertEquals(1, table.getReferenceList(abc).size());

    assertEquals("{b: {c: function (): undefined}}", a.getType().toString());
    assertEquals("{c: function (): undefined}", ab.getType().toString());
    assertEquals("function (): undefined", abc.getType().toString());
  }

// com.google.javascript.jscomp.SymbolTableTest::testJSDocOnlySymbol
  public void testJSDocOnlySymbol() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n"
        + "var a;");
    Symbol x = getDocVar(table, "x");
    assertNotNull(x);
    assertEquals("number", x.getType().toString());
    assertEquals(1, table.getReferenceList(x).size());

    Symbol y = getDocVar(table, "y");
    assertNotNull(x);
    assertEquals(null, y.getType());
    assertEquals(1, table.getReferenceList(y).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testNamespaceDefinitionOrder
  public void testNamespaceDefinitionOrder() throws Exception {
    
    
    SymbolTable table = createSymbolTable(
        " var goog = {};\n"
        + " goog.dom.Foo = function() {};\n"
        + " goog.dom = {};\n");

    Symbol goog = getGlobalVar(table, "goog");
    Symbol dom = getGlobalVar(table, "goog.dom");
    Symbol Foo = getGlobalVar(table, "goog.dom.Foo");

    assertNotNull(goog);
    assertNotNull(dom);
    assertNotNull(Foo);

    assertEquals(dom, goog.getPropertyScope().getSlot("dom"));
    assertEquals(Foo, dom.getPropertyScope().getSlot("Foo"));
  }

// com.google.javascript.jscomp.SymbolTableTest::testConstructorAlias
  public void testConstructorAlias() throws Exception {
    SymbolTable table = createSymbolTable(
        " var Foo = function() {};\n" +
        " Foo.prototype.bar = function() {};\n" +
        " var FooAlias = Foo;\n" +
        " FooAlias.prototype.baz = function() {};\n");

    Symbol foo = getGlobalVar(table, "Foo");
    Symbol fooAlias = getGlobalVar(table, "FooAlias");
    Symbol bar = getGlobalVar(table, "Foo.prototype.bar");
    Symbol baz = getGlobalVar(table, "Foo.prototype.baz");
    Symbol bazAlias = getGlobalVar(table, "FooAlias.prototype.baz");

    assertNotNull(foo);
    assertNotNull(fooAlias);
    assertNotNull(bar);
    assertNotNull(baz);
    assertNull(bazAlias);

    Symbol barScope = table.getSymbolForScope(table.getScope(bar));
    assertNotNull(barScope);

    Symbol bazScope = table.getSymbolForScope(table.getScope(baz));
    assertNotNull(bazScope);

    Symbol fooPrototype = foo.getPropertyScope().getSlot("prototype");
    assertNotNull(fooPrototype);

    assertEquals(fooPrototype, barScope);
    assertEquals(fooPrototype, bazScope);
  }

// com.google.javascript.jscomp.SymbolTableTest::testSymbolForScopeOfNatives
  public void testSymbolForScopeOfNatives() throws Exception {
    SymbolTable table = createSymbolTable("");

    
    Symbol sliceArg = getLocalVar(table, "sliceArg");
    assertNotNull(sliceArg);

    Symbol scope = table.getSymbolForScope(table.getScope(sliceArg));
    assertNotNull(scope);
    assertEquals(scope, getGlobalVar(table, "String.prototype.slice"));

    Symbol proto = getGlobalVar(table, "String.prototype");
    assertEquals(
        "externs1", proto.getDeclaration().getNode().getSourceFileName());
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
    testSame(
        "\n"
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
