// buggy code
    void maybeDeclareQualifiedName(NodeTraversal t, JSDocInfo info,
        Node n, Node parent, Node rhsValue) {
      Node ownerNode = n.getFirstChild();
      String ownerName = ownerNode.getQualifiedName();
      String qName = n.getQualifiedName();
      String propName = n.getLastChild().getString();
      Preconditions.checkArgument(qName != null && ownerName != null);

      // Precedence of type information on GETPROPs:
      // 1) @type annnotation / @enum annotation
      // 2) ASSIGN to FUNCTION literal
      // 3) @param/@return annotation (with no function literal)
      // 4) ASSIGN to something marked @const
      // 5) ASSIGN to anything else
      //
      // 1, 3, and 4 are declarations, 5 is inferred, and 2 is a declaration iff
      // the function has jsdoc or has not been declared before.
      //
      // FUNCTION literals are special because TypedScopeCreator is very smart
      // about getting as much type information as possible for them.

      // Determining type for #1 + #2 + #3 + #4
      JSType valueType = getDeclaredType(t.getSourceName(), info, n, rhsValue);
      if (valueType == null && rhsValue != null) {
        // Determining type for #5
        valueType = rhsValue.getJSType();
      }
      // Function prototypes are special.
      // It's a common JS idiom to do:
      // F.prototype = { ... };
      // So if F does not have an explicitly declared super type,
      // allow F.prototype to be redefined arbitrarily.
      if ("prototype".equals(propName)) {
        Var qVar = scope.getVar(qName);
        if (qVar != null) {
          // If the programmer has declared that F inherits from Super,
          // and they assign F.prototype to an object literal,
          // then they are responsible for making sure that the object literal's
          // implicit prototype is set up appropriately. We just obey
          // the @extends tag.
          ObjectType qVarType = ObjectType.cast(qVar.getType());
          if (qVarType != null &&
              rhsValue != null &&
              rhsValue.isObjectLit()) {
            typeRegistry.resetImplicitPrototype(
                rhsValue.getJSType(), qVarType.getImplicitPrototype());
          } else if (!qVar.isTypeInferred()) {
            // If the programmer has declared that F inherits from Super,
            // and they assign F.prototype to some arbitrary expression,
            // there's not much we can do. We just ignore the expression,
            // and hope they've annotated their code in a way to tell us
            // what props are going to be on that prototype.
            return;
          }
          if (qVar.getScope() == scope) {
            scope.undeclare(qVar);
          }
        }
      }

      if (valueType == null) {
        if (parent.isExprResult()) {
          stubDeclarations.add(new StubDeclaration(
              n,
              t.getInput() != null && t.getInput().isExtern(),
              ownerName));
        }

        return;
      }

      // NOTE(nicksantos): Determining whether a property is declared or not
      // is really really obnoxious.
      //
      // The problem is that there are two (equally valid) coding styles:
      //
      // (function() {
      //   /* The authoritative definition of goog.bar. */
      //   goog.bar = function() {};
      // })();
      //
      // function f() {
      //   goog.bar();
      //   /* Reset goog.bar to a no-op. */
      //   goog.bar = function() {};
      // }
      //
      // In a dynamic language with first-class functions, it's very difficult
      // to know which one the user intended without looking at lots of
      // contextual information (the second example demonstrates a small case
      // of this, but there are some really pathological cases as well).
      //
      // The current algorithm checks if either the declaration has
      // jsdoc type information, or @const with a known type,
      // or a function literal with a name we haven't seen before.
      boolean inferred = true;
      if (info != null) {
        // Determining declaration for #1 + #3 + #4
        inferred = !(info.hasType()
            || info.hasEnumParameterType()
            || (info.isConstant() && valueType != null
                && !valueType.isUnknownType())
            || FunctionTypeBuilder.isFunctionTypeDeclaration(info));
      }

      if (inferred) {
        // Determining declaration for #2
        inferred = !(rhsValue != null &&
            rhsValue.isFunction() &&
            (info != null || !scope.isDeclared(qName, false)));
      }

      if (!inferred) {
        ObjectType ownerType = getObjectSlot(ownerName);
        if (ownerType != null) {
          // Only declare this as an official property if it has not been
          // declared yet.
          boolean isExtern = t.getInput() != null && t.getInput().isExtern();
          if ((!ownerType.hasOwnProperty(propName) ||
               ownerType.isPropertyTypeInferred(propName)) &&
              ((isExtern && !ownerType.isNativeObjectType()) ||
               !ownerType.isInstanceType())) {
            // If the property is undeclared or inferred, declare it now.
            ownerType.defineDeclaredProperty(propName, valueType, n);
          }
        }

        // If the property is already declared, the error will be
        // caught when we try to declare it in the current scope.
        defineSlot(n, parent, valueType, inferred);
      } else if (rhsValue != null && rhsValue.isTrue()) {
        // We declare these for delegate proxy method properties.
        FunctionType ownerType =
            JSType.toMaybeFunctionType(getObjectSlot(ownerName));
        if (ownerType != null) {
          JSType ownerTypeOfThis = ownerType.getTypeOfThis();
          String delegateName = codingConvention.getDelegateSuperclassName();
          JSType delegateType = delegateName == null ?
              null : typeRegistry.getType(delegateName);
          if (delegateType != null &&
              ownerTypeOfThis.isSubtype(delegateType)) {
            defineSlot(n, parent, getNativeType(BOOLEAN_TYPE), true);
          }
        }
      }
    }

// relevant test
// com.google.javascript.jscomp.PhaseOptimizerTest::testConsumption3
  public void testConsumption3() {
    optimizer.consume(
        Lists.newArrayList(
            createPassFactory("a", 2, true),
            createPassFactory("b", 0, false),
            createPassFactory("c", 0, false)));
    assertPasses("a", "b", "c");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testDuplicateLoop
  public void testDuplicateLoop() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 1);
    try {
      addLoopedPass(loop, "x", 1);
      fail("Expected exception");
    } catch (IllegalArgumentException e) {}
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testPassOrdering
  public void testPassOrdering() {
    Loop loop = optimizer.addFixedPointLoop();
    List<String> optimalOrder = Lists.newArrayList(
        PhaseOptimizer.OPTIMAL_ORDER);
    Random random = new Random();
    while (optimalOrder.size() > 0) {
      addLoopedPass(
          loop, optimalOrder.remove(random.nextInt(optimalOrder.size())), 0);
    }
    optimizer.process(null, null);
    assertEquals(PhaseOptimizer.OPTIMAL_ORDER, passesRun);
  }

// com.google.javascript.jscomp.PrepareAstTest::testJsDocNormalization
  public void testJsDocNormalization() throws Exception {
    Node root = parseExpectedJs(
        "var x = { a: function() {}," +
        "         c:  ('d')};");
    Node objlit = root.getFirstChild().getFirstChild().getFirstChild()
        .getFirstChild();
    assertEquals(Token.OBJECTLIT, objlit.getType());

    Node firstKey = objlit.getFirstChild();
    Node firstVal = firstKey.getFirstChild();

    Node secondKey = firstKey.getNext();
    Node secondVal = secondKey.getFirstChild();
    assertNotNull(firstKey.getJSDocInfo());
    assertNotNull(firstVal.getJSDocInfo());
    assertNull(secondKey.getJSDocInfo());
    assertNotNull(secondVal.getJSDocInfo());
  }

// com.google.javascript.jscomp.PrepareAstTest::testFreeCall1
  public void testFreeCall1() throws Exception {
    Node root = parseExpectedJs("foo();");
    Node script = root.getFirstChild();
    Preconditions.checkState(script.isScript());
    Node firstExpr = script.getFirstChild();
    Node call = firstExpr.getFirstChild();
    Preconditions.checkState(call.isCall());

    assertTrue(call.getBooleanProp(Node.FREE_CALL));
  }

// com.google.javascript.jscomp.PrepareAstTest::testFreeCall2
  public void testFreeCall2() throws Exception {
    Node root = parseExpectedJs("x.foo();");
    Node script = root.getFirstChild();
    Preconditions.checkState(script.isScript());
    Node firstExpr = script.getFirstChild();
    Node call = firstExpr.getFirstChild();
    Preconditions.checkState(call.isCall());

    assertFalse(call.getBooleanProp(Node.FREE_CALL));
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

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidSetCssNameMappingWithType
  public void testValidSetCssNameMappingWithType() {
    test("goog.setCssNameMapping({foo:'bar',\"biz\":'baz'}, 'BY_PART');", "");
    CssRenamingMap map = getLastCompiler().getCssRenamingMap();
    assertNotNull(map);
    assertEquals("bar", map.get("foo"));
    assertEquals("baz", map.get("biz"));

    test("goog.setCssNameMapping({foo:'bar',biz:'baz','biz-foo':'baz-bar'}," +
        " 'BY_WHOLE');", "");
    map = getLastCompiler().getCssRenamingMap();
    assertNotNull(map);
    assertEquals("bar", map.get("foo"));
    assertEquals("baz", map.get("biz"));
    assertEquals("baz-bar", map.get("biz-foo"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSetCssNameMappingNonStringValueReturnsError
  public void testSetCssNameMappingNonStringValueReturnsError() {
    
    test("var BAR = {foo:'bar'}; goog.setCssNameMapping(BAR);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping([]);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping(false);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping(null);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping(undefined);", "",
        EXPECTED_OBJECTLIT_ERROR);

    
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

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSetCssNameMappingValidity
  public void testSetCssNameMappingValidity() {
    
    test("goog.setCssNameMapping({'a': 'b', 'a-a': 'c'})", "", null,
        INVALID_CSS_RENAMING_MAP);

    
    test("goog.setCssNameMapping({'a': 'b', 'a-a': 'c'}, 'BY_WHOLE')", "", null,
        INVALID_CSS_RENAMING_MAP);

    
    test("goog.setCssNameMapping({foo:'bar'}, 'UNKNOWN');", "",
        INVALID_STYLE_ERROR);
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

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules2b
  public void testProvideInIndependentModules2b() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo'); apps.foo = function() {};",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {};",
            "apps.foo = function() {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules3
  public void testProvideInIndependentModules3() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.B');",
            "goog.provide('apps.foo'); goog.require('apps.foo');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "apps.foo.B = {};",
            "",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules3b
  public void testProvideInIndependentModules3b() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.B');",
            "goog.provide('apps.foo'); apps.foo = function() {}; " +
            "goog.require('apps.foo');"),
        new String[] {
            "var apps = {};",
            "apps.foo.B = {};",
            "apps.foo = function() {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules4
  public void testProvideInIndependentModules4() {
    
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.bar.B');",
            "goog.provide('apps.foo.bar.C');"),
        new String[] {
            "var apps = {};apps.foo = {};apps.foo.bar = {}",
            "apps.foo.bar.B = {};",
            "apps.foo.bar.C = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireOfBaseGoog
  public void testRequireOfBaseGoog() {
    test("goog.require('goog');",
         "", MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSourcePositionPreservation
  public void testSourcePositionPreservation() {
    test("goog.provide('foo.bar.baz');",
         "var foo = {};" +
         "foo.bar = {};" +
         "foo.bar.baz = {};");

    Node root = getLastCompiler().getRoot();

    Node fooDecl = findQualifiedNameNode("foo", root);
    Node fooBarDecl = findQualifiedNameNode("foo.bar", root);
    Node fooBarBazDecl = findQualifiedNameNode("foo.bar.baz", root);

    assertEquals(1, fooDecl.getLineno());
    assertEquals(14, fooDecl.getCharno());

    assertEquals(1, fooBarDecl.getLineno());
    assertEquals(18, fooBarDecl.getCharno());

    assertEquals(1, fooBarBazDecl.getLineno());
    assertEquals(22, fooBarBazDecl.getCharno());
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoStubForProvidedTypedef
  public void testNoStubForProvidedTypedef() {
    test("goog.provide('x');  var x;", "var x;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoStubForProvidedTypedef2
  public void testNoStubForProvidedTypedef2() {
    test("goog.provide('x.y');  x.y;",
         "var x = {}; x.y;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoStubForProvidedTypedef4
  public void testNoStubForProvidedTypedef4() {
    test("goog.provide('x.y.z');  x.y.z;",
         "var x = {}; x.y = {}; x.y.z;");
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

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithBadValue1
  public void testDefineWithBadValue1() {
    test(" var DEF = new Boolean(true);", null,
        ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithBadValue2
  public void testDefineWithBadValue2() {
    test(" var DEF = 'x' + y;", null,
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

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString0
  public void testOverridingString0() {
    test(
        " var DEF_OVERRIDE_STRING = 'x';",
        "var DEF_OVERRIDE_STRING=\"x\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString1
  public void testOverridingString1() {
    test(
        " var DEF_OVERRIDE_STRING = 'x' + 'y';",
        "var DEF_OVERRIDE_STRING=\"x\" + \"y\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString2
  public void testOverridingString2() {
    overrides.put("DEF_OVERRIDE_STRING", Node.newString("foo"));
    test(
        " var DEF_OVERRIDE_STRING = 'x';",
        "var DEF_OVERRIDE_STRING=\"foo\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString3
  public void testOverridingString3() {
    overrides.put("DEF_OVERRIDE_STRING", Node.newString("foo"));
    test(
        " var DEF_OVERRIDE_STRING = 'x' + 'y';",
        "var DEF_OVERRIDE_STRING=\"foo\"");
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
    assertEquals(1, def.getRefs().size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.getDeclaration());
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
    assertEquals(2, def.getRefs().size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.getDeclaration());
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
    assertEquals(1, aDotB.getRefs().size());
    assertEquals(1, aDotB.globalSets);
    assertNotNull(aDotB.getDeclaration());
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2a
  public void testNamespacedDefine2a() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};  a.B = false;",
         "var a = {}; a.B = true;");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2b
  public void testNamespacedDefine2b() {
    
    
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {  B : false };",
         "var a = {B : false};",
         null, ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2c
  public void testNamespacedDefine2c() {
    
    
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {  get B() { return false } };",
      "var a = {get B() { return false } };",
      null, ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine3
  public void testNamespacedDefine3() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};", "var a = {};", null,
         ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine4
  public void testNamespacedDefine4() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};  a.B = false;",
         "var a = {}; a.B = true;");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverrideAfterAlias
  public void testOverrideAfterAlias() {
    test("var x; var DEF=true; x=DEF; DEF=false;",
         null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testBasicTweak1
  public void testBasicTweak1() {
    testSame("goog.tweak.registerBoolean('Foo', 'Description');" +
        "goog.tweak.getBoolean('Foo')");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testBasicTweak2
  public void testBasicTweak2() {
    testSame("goog.tweak.registerString('Foo', 'Description');" +
        "goog.tweak.getString('Foo')");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testBasicTweak3
  public void testBasicTweak3() {
    testSame("goog.tweak.registerNumber('Foo', 'Description');" +
        "goog.tweak.getNumber('Foo')");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testBasicTweak4
  public void testBasicTweak4() {
    testSame("goog.tweak.registerButton('Foo', 'Description', function() {})");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testBasicTweak5
  public void testBasicTweak5() {
    testSame("goog.tweak.registerBoolean('A.b_7', 'Description', true, " +
        "{ requiresRestart:false })");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testBasicTweak6
  public void testBasicTweak6() {
    testSame("var opts = { requiresRestart:false };" +
        "goog.tweak.registerBoolean('Foo', 'Description', true, opts)");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testNonLiteralId1
  public void testNonLiteralId1() {
    test("goog.tweak.registerBoolean(3, 'Description')", null,
         ProcessTweaks.NON_LITERAL_TWEAK_ID_ERROR);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testNonLiteralId2
  public void testNonLiteralId2() {
    test("goog.tweak.getBoolean('a' + 'b')", null,
         ProcessTweaks.NON_LITERAL_TWEAK_ID_ERROR);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testNonLiteralId3
  public void testNonLiteralId3() {
    test("var CONST = 'foo'; goog.tweak.overrideDefaultValue(CONST, 3)", null,
        ProcessTweaks.NON_LITERAL_TWEAK_ID_ERROR);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testInvalidId
  public void testInvalidId() {
    test("goog.tweak.registerBoolean('Some ID', 'a')", null,
        ProcessTweaks.INVALID_TWEAK_ID_ERROR);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testInvalidDefaultValue1
  public void testInvalidDefaultValue1() {
    testSame("var val = true; goog.tweak.registerBoolean('Foo', 'desc', val)",
         ProcessTweaks.INVALID_TWEAK_DEFAULT_VALUE_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testInvalidDefaultValue2
  public void testInvalidDefaultValue2() {
    testSame("goog.tweak.overrideDefaultValue('Foo', 3 + 1);" +
        "goog.tweak.registerNumber('Foo', 'desc')",
        ProcessTweaks.INVALID_TWEAK_DEFAULT_VALUE_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testUnknownGetString
  public void testUnknownGetString() {
    testSame("goog.tweak.getString('huh')",
        ProcessTweaks.UNKNOWN_TWEAK_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testUnknownGetNumber
  public void testUnknownGetNumber() {
    testSame("goog.tweak.getNumber('huh')",
        ProcessTweaks.UNKNOWN_TWEAK_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testUnknownGetBoolean
  public void testUnknownGetBoolean() {
    testSame("goog.tweak.getBoolean('huh')",
        ProcessTweaks.UNKNOWN_TWEAK_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testUnknownOverride
  public void testUnknownOverride() {
    testSame("goog.tweak.overrideDefaultValue('huh', 'val')",
        ProcessTweaks.UNKNOWN_TWEAK_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testDuplicateTweak
  public void testDuplicateTweak() {
    test("goog.tweak.registerBoolean('TweakA', 'desc');" +
        "goog.tweak.registerBoolean('TweakA', 'desc')", null,
        ProcessTweaks.TWEAK_MULTIPLY_REGISTERED_ERROR);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testOverrideAfterRegister
  public void testOverrideAfterRegister() {
    test("goog.tweak.registerBoolean('TweakA', 'desc');" +
        "goog.tweak.overrideDefaultValue('TweakA', 'val')",
         null, ProcessTweaks.TWEAK_OVERRIDE_AFTER_REGISTERED_ERROR);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testRegisterInNonGlobalScope
  public void testRegisterInNonGlobalScope() {
    test("function foo() {goog.tweak.registerBoolean('TweakA', 'desc');};",
        null, ProcessTweaks.NON_GLOBAL_TWEAK_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testWrongGetter1
  public void testWrongGetter1() {
    testSame("goog.tweak.registerBoolean('TweakA', 'desc');" +
        "goog.tweak.getString('TweakA')",
        ProcessTweaks.TWEAK_WRONG_GETTER_TYPE_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testWrongGetter2
  public void testWrongGetter2() {
    testSame("goog.tweak.registerString('TweakA', 'desc');" +
        "goog.tweak.getNumber('TweakA')",
        ProcessTweaks.TWEAK_WRONG_GETTER_TYPE_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testWrongGetter3
  public void testWrongGetter3() {
    testSame("goog.tweak.registerNumber('TweakA', 'desc');" +
        "goog.tweak.getBoolean('TweakA')",
        ProcessTweaks.TWEAK_WRONG_GETTER_TYPE_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testWithNoTweaks
  public void testWithNoTweaks() {
    testSame("var DEF=true;var x={};x.foo={}");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testStrippingWithImplicitDefaultValues
  public void testStrippingWithImplicitDefaultValues() {
    stripTweaks = true;
    test("goog.tweak.registerNumber('TweakA', 'desc');" +
        "goog.tweak.registerBoolean('TweakB', 'desc');" +
        "goog.tweak.registerString('TweakC', 'desc');" +
        "alert(goog.tweak.getNumber('TweakA'));" +
        "alert(goog.tweak.getBoolean('TweakB'));" +
        "alert(goog.tweak.getString('TweakC'));",
        "void 0; void 0; void 0; alert(0); alert(false); alert('')");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testStrippingWithExplicitDefaultValues
  public void testStrippingWithExplicitDefaultValues() {
    stripTweaks = true;
    test("goog.tweak.registerNumber('TweakA', 'desc', 5);" +
        "goog.tweak.registerBoolean('TweakB', 'desc', true);" +
        "goog.tweak.registerString('TweakC', 'desc', '!');" +
        "alert(goog.tweak.getNumber('TweakA'));" +
        "alert(goog.tweak.getBoolean('TweakB'));" +
        "alert(goog.tweak.getString('TweakC'));",
        "void 0; void 0; void 0; alert(5); alert(true); alert('!')");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testStrippingWithInCodeOverrides
  public void testStrippingWithInCodeOverrides() {
    stripTweaks = true;
    test("goog.tweak.overrideDefaultValue('TweakA', 5);" +
        "goog.tweak.overrideDefaultValue('TweakB', true);" +
        "goog.tweak.overrideDefaultValue('TweakC', 'bar');" +
        "goog.tweak.registerNumber('TweakA', 'desc');" +
        "goog.tweak.registerBoolean('TweakB', 'desc');" +
        "goog.tweak.registerString('TweakC', 'desc', 'foo');" +
        "alert(goog.tweak.getNumber('TweakA'));" +
        "alert(goog.tweak.getBoolean('TweakB'));" +
        "alert(goog.tweak.getString('TweakC'));",
        "void 0; void 0; void 0; void 0; void 0; void 0;" +
        "alert(5); alert(true); alert('bar');");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testStrippingWithUnregisteredTweak1
  public void testStrippingWithUnregisteredTweak1() {
    stripTweaks = true;
    test("alert(goog.tweak.getNumber('TweakA'));",
        "alert(0)", null, ProcessTweaks.UNKNOWN_TWEAK_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testStrippingWithUnregisteredTweak2
  public void testStrippingWithUnregisteredTweak2() {
    stripTweaks = true;
    test("alert(goog.tweak.getBoolean('TweakB'))",
        "alert(false)", null, ProcessTweaks.UNKNOWN_TWEAK_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testStrippingWithUnregisteredTweak3
  public void testStrippingWithUnregisteredTweak3() {
    stripTweaks = true;
    test("alert(goog.tweak.getString('TweakC'))",
        "alert('')", null, ProcessTweaks.UNKNOWN_TWEAK_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testStrippingOfManuallyRegistered1
  public void testStrippingOfManuallyRegistered1() {
    stripTweaks = true;
    test("var reg = goog.tweak.getRegistry();" +
         "if (reg) {" +
         "  reg.register(new goog.tweak.BooleanSetting('foo', 'desc'));" +
         "  reg.getEntry('foo').setDefaultValue(1);" +
         "}",
         "if (null);");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testOverridesWithStripping
  public void testOverridesWithStripping() {
    stripTweaks = true;
    defaultValueOverrides.put("TweakA", Node.newNumber(1));
    defaultValueOverrides.put("TweakB", new Node(Token.FALSE));
    defaultValueOverrides.put("TweakC", Node.newString("!"));
    test("goog.tweak.overrideDefaultValue('TweakA', 5);" +
        "goog.tweak.overrideDefaultValue('TweakC', 'bar');" +
        "goog.tweak.registerNumber('TweakA', 'desc');" +
        "goog.tweak.registerBoolean('TweakB', 'desc', true);" +
        "goog.tweak.registerString('TweakC', 'desc', 'foo');" +
        "alert(goog.tweak.getNumber('TweakA'));" +
        "alert(goog.tweak.getBoolean('TweakB'));" +
        "alert(goog.tweak.getString('TweakC'));",
        "void 0; void 0; void 0; void 0; void 0; " +
        "alert(1); alert(false); alert('!')");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testCompilerOverridesNoStripping1
  public void testCompilerOverridesNoStripping1() {
    defaultValueOverrides.put("TweakA", Node.newNumber(1));
    defaultValueOverrides.put("TweakB", new Node(Token.FALSE));
    defaultValueOverrides.put("TweakC", Node.newString("!"));
    test("goog.tweak.registerNumber('TweakA', 'desc');" +
        "goog.tweak.registerBoolean('TweakB', 'desc', true);" +
        "goog.tweak.registerString('TweakC', 'desc', 'foo');" +
        "var a = goog.tweak.getCompilerOverrides_()",
        "goog.tweak.registerNumber('TweakA', 'desc');" +
        "goog.tweak.registerBoolean('TweakB', 'desc', true);" +
        "goog.tweak.registerString('TweakC', 'desc', 'foo');" +
        "var a = { TweakA: 1, TweakB: false, TweakC: '!' };");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testCompilerOverridesNoStripping2
  public void testCompilerOverridesNoStripping2() {
    defaultValueOverrides.put("TweakA", Node.newNumber(1));
    defaultValueOverrides.put("TweakB", new Node(Token.FALSE));
    defaultValueOverrides.put("TweakC", Node.newString("!"));
    test("goog.tweak.registerNumber('TweakA', 'desc');" +
        "goog.tweak.registerBoolean('TweakB', 'desc', true);" +
        "goog.tweak.registerString('TweakC', 'desc', 'foo');" +
        "var a = goog.tweak.getCompilerOverrides_();" +
        "var b = goog.tweak.getCompilerOverrides_()",
        "goog.tweak.registerNumber('TweakA', 'desc');" +
        "goog.tweak.registerBoolean('TweakB', 'desc', true);" +
        "goog.tweak.registerString('TweakC', 'desc', 'foo');" +
        "var a = { TweakA: 1, TweakB: false, TweakC: '!' };" +
        "var b = { TweakA: 1, TweakB: false, TweakC: '!' };");
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testUnknownCompilerOverride
  public void testUnknownCompilerOverride() {
    allowSourcelessWarnings();
    defaultValueOverrides.put("TweakA", Node.newString("!"));
    testSame("var a", ProcessTweaks.UNKNOWN_TWEAK_WARNING);
  }

// com.google.javascript.jscomp.ProcessTweaksTest::testCompilerOverrideWithWrongType
  public void testCompilerOverrideWithWrongType() {
    allowSourcelessWarnings();
    defaultValueOverrides.put("TweakA", Node.newString("!"));
    testSame("goog.tweak.registerBoolean('TweakA', 'desc')",
        ProcessTweaks.INVALID_TWEAK_DEFAULT_VALUE_WARNING);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testIssue303
  public void testIssue303() throws Exception {
    checkMarkedCalls(
        " function F() {" +
        "  var self = this;" +
        "  window.setTimeout(function() {" +
        "    window.location = self.location;" +
        "  }, 0);" +
        "}" +
        "F.prototype.setLocation = function(x) {" +
        "  this.location = x;" +
        "};" +
        "(new F()).setLocation('http://www.google.com/');",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testIssue303b
  public void testIssue303b() throws Exception {
    checkMarkedCalls(
        " function F() {" +
        "  var self = this;" +
        "  window.setTimeout(function() {" +
        "    window.location = self.location;" +
        "  }, 0);" +
        "}" +
        "F.prototype.setLocation = function(x) {" +
        "  this.location = x;" +
        "};" +
        "function x() {" +
        "  (new F()).setLocation('http://www.google.com/');" +
        "} window['x'] = x;",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new1
  public void testAnnotationInExterns_new1() throws Exception {
    checkMarkedCalls("externSENone()",
        ImmutableList.<String>of("externSENone"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new2
  public void testAnnotationInExterns_new2() throws Exception {
    checkMarkedCalls("externSEThis()",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new3
  public void testAnnotationInExterns_new3() throws Exception {
    checkMarkedCalls("new externObjSEThis()",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new4
  public void testAnnotationInExterns_new4() throws Exception {
    
    

    checkMarkedCalls("new externObjSEThis().externObjSEThisMethod('')",
        ImmutableList.<String>of(
           "externObjSEThis", "NEW STRING externObjSEThisMethod"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new5
  public void testAnnotationInExterns_new5() throws Exception {
    checkMarkedCalls(
        "function f() { new externObjSEThis() };" +
        "f();",
        ImmutableList.<String>of("externObjSEThis", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new6
  public void testAnnotationInExterns_new6() throws Exception {
    
    
    
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  new externObjSEThis().externObjSEThisMethod('') " +
        "};" +
        "f();",
         ImmutableList.<String>of(
             "externObjSEThis", "NEW STRING externObjSEThisMethod"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new7
  public void testAnnotationInExterns_new7() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  var x = new externObjSEThis(); " +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f();",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new8
  public void testAnnotationInExterns_new8() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f(x) {" +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f(new externObjSEThis());",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new9
  public void testAnnotationInExterns_new9() throws Exception {
    
    
    
    
    checkMarkedCalls(
        "function f(x) {" +
        "  x = new externObjSEThis(); " +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f(g);",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new10
  public void testAnnotationInExterns_new10() throws Exception {
    
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  new externObjSEThis().externObjSEThisMethod2('') " +
        "};" +
        "f();",
        ImmutableList.<String>of("externObjSEThis"));
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

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs1
  public void testAnnotationInExternStubs1() throws Exception {
    checkMarkedCalls("o.propWithStubBefore('a');",
        ImmutableList.<String>of("o.propWithStubBefore"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs1b
  public void testAnnotationInExternStubs1b() throws Exception {
    checkMarkedCalls("o.propWithStubBeforeWithJSDoc('a');",
        ImmutableList.<String>of("o.propWithStubBeforeWithJSDoc"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs2
  public void testAnnotationInExternStubs2() throws Exception {
    checkMarkedCalls("o.propWithStubAfter('a');",
        ImmutableList.<String>of("o.propWithStubAfter"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs2b
  public void testAnnotationInExternStubs2b() throws Exception {
    checkMarkedCalls("o.propWithStubAfter('a');",
        ImmutableList.<String>of("o.propWithStubAfter"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs3
  public void testAnnotationInExternStubs3() throws Exception {
    checkMarkedCalls("propWithAnnotatedStubAfter('a');",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs4
  public void testAnnotationInExternStubs4() throws Exception {
    
    
    String externs =
      "function externObj5(){}\n" +

      "externObj5.prototype.propWithAnnotatedStubAfter = function(s) {};\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter;\n";

    List<String> expected = ImmutableList.<String>of();
    testSame(externs,
        "o.prototype.propWithAnnotatedStubAfter",
        TypeValidator.DUP_VAR_DECLARATION, false);
    assertEquals(expected, noSideEffectCalls);
    noSideEffectCalls.clear();
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs5
  public void testAnnotationInExternStubs5() throws Exception {
    
    
    String externs =
      "function externObj5(){}\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter = function(s) {};\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter;\n";

    List<String> expected = ImmutableList.<String>of();
    testSame(externs,
        "o.prototype.propWithAnnotatedStubAfter",
        TypeValidator.DUP_VAR_DECLARATION, false);
    assertEquals(expected, noSideEffectCalls);
    noSideEffectCalls.clear();
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

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testResultLocalitySimple
  public void testResultLocalitySimple() throws Exception {
    String prefix = "var g; function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");
    List<String> notExpected = ImmutableList.of();

    
    checkLocalityOfMarkedCalls(
        prefix + "" + suffix, expected);
    
    checkLocalityOfMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkLocalityOfMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return g" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return 1; return 2" + suffix, expected);
    checkLocalityOfMarkedCalls(
        prefix + "return 1; return g" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; return a" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, notExpected);
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return {foo : 1}.foo" + suffix,
        notExpected);
    checkLocalityOfMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix,
        notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return externObj" + suffix, notExpected);
    checkLocalityOfMarkedCalls(
        "function inner(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, notExpected);
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

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects1
  public void testLocalizedSideEffects1() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {foo : 0}; return function() {x.foo++};" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects2
  public void testLocalizedSideEffects2() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {foo : 0}; (function() {x.foo++})();" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects3
  public void testLocalizedSideEffects3() throws Exception {
    
    
    checkMarkedCalls("var g = {foo:1}; function f() {var x = g; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects4
  public void testLocalizedSideEffects4() throws Exception {
    
    
    checkMarkedCalls("function f() {var x = []; x[0] = 1;}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects5
  public void testLocalizedSideEffects5() throws Exception {
    
    
    checkMarkedCalls("var g = [];function f() {var x = g; x[0] = 1;}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects6
  public void testLocalizedSideEffects6() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {}; x.foo = 1; return x;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects7
  public void testLocalizedSideEffects7() throws Exception {
    
    
    checkMarkedCalls(" function A() {};" +
                     "function f() {" +
                     "  var a = []; a[1] = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects8
  public void testLocalizedSideEffects8() throws Exception {
    
    
    
    checkMarkedCalls(" function A() {};" +
                     "function f() {" +
                     "  var a = new A; a.foo = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects9
  public void testLocalizedSideEffects9() throws Exception {
    
    
    
    checkMarkedCalls(" function A() {this.x = 1};" +
                     "function f() {" +
                     "  var a = new A; a.foo = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects10
  public void testLocalizedSideEffects10() throws Exception {
    
    
    checkMarkedCalls(" function A() {};" +
                     "A.prototype.g = function() {this.x = 1};" +
                     "function f() {" +
                     "  var a = new A; a.g(); return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects11
  public void testLocalizedSideEffects11() throws Exception {
    
    checkMarkedCalls(
        " function A() {}" +
        "A.prototype.update = function() { this.x = 1; };" +
        " function B() { " +
        "  this.a_ = new A();" +
        "}" +
        "B.prototype.updateA = function() {" +
        "  var b = this.a_;" +
        "  b.update();" +
        "};" +
        "var x = new B();" +
        "x.updateA();",
        ImmutableList.of("A", "B"));
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
                     ImmutableList.<String>of("f"));
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

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallRegExpWithSideEffects
  public void testCallRegExpWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function k(){(/a/).exec('')}\n" +
        "k()";

    regExpHaveSideEffects = true;
    checkMarkedCalls(source, ImmutableList.<String>of());
    regExpHaveSideEffects = false;
    checkMarkedCalls(source, ImmutableList.<String>of(
        "REGEXP STRING exec", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction1
  public void testAnonymousFunction1() throws Exception {
    String source = "(function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "FUNCTION"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction2
  public void testAnonymousFunction2() throws Exception {
    String source = "(Error || function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "(Error || FUNCTION)"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction3
  public void testAnonymousFunction3() throws Exception {
    String source = "var a = (Error || function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "(Error || FUNCTION)"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction4
  public void testAnonymousFunction4() throws Exception {
    String source = "var a = (Error || function (){});" +
                    "a();";

    
    checkMarkedCalls(source, ImmutableList.<String>of());
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

// com.google.javascript.jscomp.RecordFunctionInformationTest::testFunction
  public void testFunction() {
    String g = "function g(){}";
    String fAndG = "function f(){" + g + "}";
    String js = "var h=" + fAndG + ";h()";

    FunctionInformationMap.Builder expected =
        FunctionInformationMap.newBuilder();
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(0)
        .setSourceName("testcode")
        .setLineNumber(1)
        .setModuleName("")
        .setSize(g.length())
        .setName("f::g")
        .setCompiledSource(g).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(1)
        .setSourceName("testcode")
        .setLineNumber(1)
        .setModuleName("")
        .setSize(fAndG.length())
        .setName("f")
        .setCompiledSource(fAndG).build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("")
        .setCompiledSource(js + ";").build());

    test(js, expected.build());
  }

// com.google.javascript.jscomp.RecordFunctionInformationTest::testModule
  public void testModule() {
    String g = "function g(){}";
    String fAndG = "function f(){" + g + "}";
    String m0_js = "var h=" + fAndG + ";h()";
    String sum = "function(a,b){return a+b}";
    String m1_js = "var x=" + sum + "(1,2)";

    FunctionInformationMap.Builder expected =
        FunctionInformationMap.newBuilder();
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(0)
        .setSourceName("i0")
        .setLineNumber(1)
        .setModuleName("m0")
        .setSize(g.length())
        .setName("f::g")
        .setCompiledSource(g).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(1)
        .setSourceName("i0")
        .setLineNumber(1)
        .setModuleName("m0")
        .setSize(fAndG.length())
        .setName("f")
        .setCompiledSource(fAndG).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(2)
        .setSourceName("i1")
        .setLineNumber(1)
        .setModuleName("m1")
        .setSize(sum.length())
        .setName("<anonymous>")
        .setCompiledSource(sum).build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("m0")
        .setCompiledSource(m0_js + ";").build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("m1")
        .setCompiledSource(m1_js + ";").build());

    test(CompilerTestCase.createModules(m0_js, m1_js), expected.build());
  }

// com.google.javascript.jscomp.RecordFunctionInformationTest::testMotionPreservesOriginalSourceName
  public void testMotionPreservesOriginalSourceName() {
    String f = "function f(){}";
    String g = "function g(){}";

    String m0_before = f + g;
    String m1_before = "";

    JSModule[] modules = CompilerTestCase.createModules(m0_before, m1_before);
    Compiler compiler = compilerFor(modules);
    Node root = root(compiler);
    Node externsRoot = externs(root);
    Node mainRoot = main(root);

    String m0_after = f;
    String m1_after = g;
    Node nodeG = mainRoot.getFirstChild().getLastChild();
    mainRoot.getFirstChild().removeChild(nodeG);
    mainRoot.getLastChild().addChildrenToBack(nodeG.cloneTree());

    FunctionInformationMap.Builder expected =
      FunctionInformationMap.newBuilder();
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(0)
        .setSourceName("i0")
        .setLineNumber(1)
        .setModuleName("m0")
        .setSize(g.length())
        .setName("f")
        .setCompiledSource(f).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(1)
        .setSourceName("i0")
        .setLineNumber(1)
        .setModuleName("m1")
        .setSize(g.length())
        .setName("g")
        .setCompiledSource(g).build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("m0")
        .setCompiledSource(m0_after + ";").build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("m1")
        .setCompiledSource(m1_after + ";").build());

    test(compiler, externsRoot, mainRoot, expected.build());
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryCatch
  public void testRemoveTryCatch() {
    test("try{var a=1;}catch(ex){var b=2;}",
         "var b;var a=1");
    test("try{var a=1;var b=2}catch(ex){var c=3;var d=4;}",
         "var d;var c;var a=1;var b=2");
    test("try{var a=1;var b=2}catch(ex){}",
         "var a=1;var b=2");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryFinally
  public void testRemoveTryFinally() {
    test("try{var a=1;}finally{var c=3;}",
         "var a=1;var c=3");
    test("try{var a=1;var b=2}finally{var e=5;var f=6;}",
         "var a=1;var b=2;var e=5;var f=6");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryCatchFinally
  public void testRemoveTryCatchFinally() {
    test("try{var a=1;}catch(ex){var b=2;}finally{var c=3;}",
         "var b;var a=1;var c=3");
    test("try{var a=1;var b=2}catch(ex){var c=3;var d=4;}finally{var e=5;" +
         "var f=6;}",
         "var d;var c;var a=1;var b=2;var e=5;var f=6");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testPreserveTryBlockContainingReturnStatement
  public void testPreserveTryBlockContainingReturnStatement() {
    testSame("function f(){var a;try{a=1;return}finally{a=2}}");
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
         "" +
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

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAnalyzePrototypeProperties
  public void testAnalyzePrototypeProperties() {
    
    test("function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.b = function(){};" +
           "var x = new e; x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           "var x = new e; x.a()");

    
    test("function e(){}" +
           "e.prototype = {a: function(){}, b: function(){}};" +
           "var x=new e; x.a()",
         "function e(){}" +
           "e.prototype = {a: function(){}};" +
           "var x = new e; x.a()");

    
    
    test("function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e; x.a()");
    test("function e(){}" +
           "e.prototype = {a: function(){}, bExtern: function(){}};" +
           "var x = new e; x.a()",
         "function e(){}" +
           "e.prototype = {a: function(){}, bExtern: function(){}};" +
           "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing1
  public void testAliasing1() {
    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "e.prototype.alias2 = e.prototype.method2;" +
           "var x = new e; x.method1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "var x = new e; x.method1()");

    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "e.prototype.alias2 = e.prototype.method2;" +
           "var x=new e; x.alias1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.alias1 = e.prototype.method1;" +
           "var x = new e; x.alias1()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing2
  public void testAliasing2() {
    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "(new e).method1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "(new e).method1()");

    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "(new e).alias1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.alias1 = e.prototype.method1;" +
           "(new e).alias1()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing3
  public void testAliasing3() {
    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype['alias1'] = e.prototype.method1;" +
           "e.prototype['alias2'] = e.prototype.method2;",
         "function e(){}" +
           "e.prototype.method1=function(){};" +
           "e.prototype.method2=function(){};" +
           "e.prototype[\"alias1\"]=e.prototype.method1;" +
           "e.prototype[\"alias2\"]=e.prototype.method2;");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing4
  public void testAliasing4() {
    
    test("function e(){}" +
           "e.prototype['alias1'] = e.prototype.method1 = function(){};" +
           "e.prototype['alias2'] = e.prototype.method2 = function(){};",
         "function e(){}" +
           "e.prototype[\"alias1\"]=e.prototype.method1=function(){};" +
           "e.prototype[\"alias2\"]=e.prototype.method2=function(){};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing5
  public void testAliasing5() {
    
    
    test("function e(){}" +
           "e.prototype.method1 = function(){this.method2()};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype['alias1'] = e.prototype.method1;",
         "function e(){}" +
           "e.prototype.method1=function(){this.method2()};" +
           "e.prototype.method2=function(){};" +
           "e.prototype[\"alias1\"]=e.prototype.method1;");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing6
  public void testAliasing6() {
    
    
    test("function e(){}" +
           "e.prototype.method1 = function(){this.method2()};" +
           "e.prototype.method2 = function(){};" +
           
           "window['alias1'] = e.prototype.method1;",
         "function e(){}" +
           "e.prototype.method1=function(){this.method2()};" +
           "e.prototype.method2=function(){};" +
           "window['alias1']=e.prototype.method1;");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing7
  public void testAliasing7() {
    
    
    testSame("function e(){}" +
           "e.prototype['alias1'] = e.prototype.method1 = " +
               "function(){this.method2()};" +
           "e.prototype.method2 = function(){};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testStatementRestriction
  public void testStatementRestriction() {
    test("function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.method = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var _externInstance = new Foo();" +
        "Foo.prototype._externMethod = Foo.prototype.method";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.method = function(){};" +
        "var _externInstance = new Foo;" +
        "Foo.prototype._externMethod = Foo.prototype.method";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testMethodsFromExternsFileNotExported
  public void testMethodsFromExternsFileNotExported() {
    canRemoveExterns = true;
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.bar_ = function() {};" +
        "Foo.prototype.unused = function() {};" +
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";

    String compiled =
        "function Foo(){}" +
        "var instance = new Foo;";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    canRemoveExterns = true;
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.method = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var _externInstance = new Foo();" +
        "Foo.prototype._externMethod = Foo.prototype.method";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.method = function(){};" +
        "var _externInstance = new Foo;" +
        "Foo.prototype._externMethod = Foo.prototype.method";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExternMethodsFromExternsFile
  public void testExternMethodsFromExternsFile() {
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.bar_ = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.bar_ = function(){};" +
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }
