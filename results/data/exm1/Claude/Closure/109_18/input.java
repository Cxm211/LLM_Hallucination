// buggy code
  private Node parseContextTypeExpression(JsDocToken token) {
          return parseTypeName(token);
  }

// relevant test
// com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalQualifiedNameInLocalScope
  public void testGlobalQualifiedNameInLocalScope() {
    testSame(
        "var ns = {}; " +
        "(function() { " +
        "     ns.foo = function(x) {}; })();" +
        "(function() { ns.foo(3); })();");
    assertNotNull(globalScope.getVar("ns.foo"));
    assertEquals(
        "function (number): undefined",
        globalScope.getVar("ns.foo").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty1
  public void testDeclaredObjectLitProperty1() throws Exception {
    testSame("var x = { y: 3};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "number",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty2
  public void testDeclaredObjectLitProperty2() throws Exception {
    testSame("var x = { y: function(z){}};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "function (number): undefined",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: function (number): undefined}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty3
  public void testDeclaredObjectLitProperty3() throws Exception {
    testSame("function f() {" +
        "  var x = { y: function(z){ return 3; }};" +
        "}");
    ObjectType xType = ObjectType.cast(lastLocalScope.getVar("x").getType());
    assertEquals(
        "function (?): number",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: function (?): number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty4
  public void testDeclaredObjectLitProperty4() throws Exception {
    testSame("var x = {y: 5,  z: 3};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "number", xType.getPropertyType("y").toString());
    assertFalse(xType.isPropertyTypeDeclared("y"));
    assertTrue(xType.isPropertyTypeDeclared("z"));
    assertEquals(
        "{y: number, z: number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty5
  public void testDeclaredObjectLitProperty5() throws Exception {
    testSame("var x = { prop: 3};" +
             "function f() { var y = x.prop; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("number", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty6
  public void testDeclaredObjectLitProperty6() throws Exception {
    testSame("var x = { prop: function(){}};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("function (): undefined", propType.toString());
    assertFalse(prop.isTypeInferred());
    assertFalse(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredObjectLitProperty1
  public void testInferredObjectLitProperty1() throws Exception {
    testSame("var x = {prop: 3};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("number", propType.toString());
    assertTrue(prop.isTypeInferred());
    assertTrue(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredObjectLitProperty2
  public void testInferredObjectLitProperty2() throws Exception {
    testSame("var x = {prop: function(){}};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("function (): undefined", propType.toString());
    assertTrue(prop.isTypeInferred());
    assertTrue(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType1
  public void testDeclaredConstType1() throws Exception {
    testSame(
        " var x = 3;" +
        "function f() { var y = x; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("number", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType2
  public void testDeclaredConstType2() throws Exception {
    testSame(
        " var x = {};" +
        "function f() { var y = x; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("{}", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType3
  public void testDeclaredConstType3() throws Exception {
    testSame(
        " var x = {};" +
        " x.z = 'hi';" +
        "function f() { var y = x.z; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("string", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType4
  public void testDeclaredConstType4() throws Exception {
    testSame(
        " function Foo() {}" +
        " Foo.prototype.z = 'hi';" +
        "function f() { var y = (new Foo()).z; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("string", yType.toString());

    ObjectType fooType =
        ((FunctionType) globalScope.getVar("Foo").getType()).getInstanceType();
    assertTrue(fooType.isPropertyTypeDeclared("z"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType5
  public void testDeclaredConstType5() throws Exception {
    testSame(
        " var goog = goog || {};" +
        " var foo = goog || {};" +
        "function f() { var y = goog; var z = foo; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("{}", yType.toString());

    JSType zType = lastLocalScope.getVar("z").getType();
    assertEquals("?", zType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadCtorInit1
  public void testBadCtorInit1() throws Exception {
    testSame(" var f;", CTOR_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadCtorInit2
  public void testBadCtorInit2() throws Exception {
    testSame("var x = {};  x.f;", CTOR_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadIfaceInit1
  public void testBadIfaceInit1() throws Exception {
    testSame(" var f;", IFACE_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadIfaceInit2
  public void testBadIfaceInit2() throws Exception {
    testSame("var x = {};  x.f;", IFACE_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInHook
  public void testFunctionInHook() throws Exception {
    testSame(" var f = Math.random() ? " +
        "function(x) {} : function(x) {};");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInAnd
  public void testFunctionInAnd() throws Exception {
    testSame(" var f = Math.random() && " +
        "function(x) {};");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInOr
  public void testFunctionInOr() throws Exception {
    testSame(" var f = Math.random() || " +
        "function(x) {};");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInComma
  public void testFunctionInComma() throws Exception {
    testSame(" var f = (Math.random(), " +
        "function(x) {});");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredCatchExpression1
  public void testDeclaredCatchExpression1() {
    testSame(
        "try {} catch (e) {}");
    
    
    assertEquals(null, globalScope.getVar("e").getType());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredCatchExpression2
  public void testDeclaredCatchExpression2() {
    testSame(
        "try {} catch ( e) {}");
    
    
    assertEquals("string", globalScope.getVar("e").getType().toString());
  }

// com.google.javascript.jscomp.VarCheckTest::testBreak
  public void testBreak() {
    testSame("a: while(1) break a;");
  }

// com.google.javascript.jscomp.VarCheckTest::testContinue
  public void testContinue() {
    testSame("a: while(1) continue a;");
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarNotDefined
  public void testReferencedVarNotDefined() {
    test("x = 0;", null, VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarDefined1
  public void testReferencedVarDefined1() {
    testSame("var x, y; x=1;");
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarDefined2
  public void testReferencedVarDefined2() {
    testSame("var x; function y() {x=1;}");
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarsExternallyDefined
  public void testReferencedVarsExternallyDefined() {
    testSame("var x = window; alert(x);");
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars1
  public void testMultiplyDeclaredVars1() {
    test("var x = 1; var x = 2;", null,
        VarCheck.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars2
  public void testMultiplyDeclaredVars2() {
    test("var y; try { y=1 } catch (x) {}" +
         "try { y=1 } catch (x) {}",
         "var y;try{y=1}catch(x){}try{y=1}catch(x){}");
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars3
  public void testMultiplyDeclaredVars3() {
    test("try { var x = 1; x *=2; } catch (x) {}", null,
         VarCheck.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars4
  public void testMultiplyDeclaredVars4() {
    testSame("x;", "var x = 1; var x = 2;",
        VarCheck.VAR_MULTIPLY_DECLARED_ERROR, true);
  }

// com.google.javascript.jscomp.VarCheckTest::testVarReferenceInExterns
  public void testVarReferenceInExterns() {
    testSame("asdf;", "var asdf;",
        VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testCallInExterns
  public void testCallInExterns() {
    testSame("yz();", "function yz() {}",
        VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testPropReferenceInExterns1
  public void testPropReferenceInExterns1() {
    testSame("asdf.foo;", "var asdf;",
        VarCheck.UNDEFINED_EXTERN_VAR_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testPropReferenceInExterns2
  public void testPropReferenceInExterns2() {
    testSame("asdf.foo;", "",
        VarCheck.UNDEFINED_VAR_ERROR, true);
  }

// com.google.javascript.jscomp.VarCheckTest::testPropReferenceInExterns3
  public void testPropReferenceInExterns3() {
    testSame("asdf.foo;", "var asdf;",
        VarCheck.UNDEFINED_EXTERN_VAR_ERROR);

    externValidationErrorLevel = CheckLevel.ERROR;
    test(
        "asdf.foo;", "var asdf;", "",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR, null);

    externValidationErrorLevel = CheckLevel.OFF;
    test("asdf.foo;", "var asdf;", "var asdf;", null, null);
  }

// com.google.javascript.jscomp.VarCheckTest::testVarInWithBlock
  public void testVarInWithBlock() {
    test("var a = {b:5}; with (a){b;}", null, VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testValidFunctionExpr
  public void testValidFunctionExpr() {
    testSame("(function() {});");
  }

// com.google.javascript.jscomp.VarCheckTest::testRecursiveFunction
  public void testRecursiveFunction() {
    testSame("(function a() { return a(); })();");
  }

// com.google.javascript.jscomp.VarCheckTest::testRecursiveFunction2
  public void testRecursiveFunction2() {
    testSame("var a = 3; (function a() { return a(); })();");
  }

// com.google.javascript.jscomp.VarCheckTest::testLegalVarReferenceBetweenModules
  public void testLegalVarReferenceBetweenModules() {
    testDependentModules("var x = 10;", "var y = x++;", null);
  }

// com.google.javascript.jscomp.VarCheckTest::testMissingModuleDependencyDefault
  public void testMissingModuleDependencyDefault() {
    testIndependentModules("var x = 10;", "var y = x++;",
                           null, VarCheck.MISSING_MODULE_DEP_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testViolatedModuleDependencyDefault
  public void testViolatedModuleDependencyDefault() {
    testDependentModules("var y = x++;", "var x = 10;",
                         VarCheck.VIOLATED_MODULE_DEP_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testMissingModuleDependencySkipNonStrict
  public void testMissingModuleDependencySkipNonStrict() {
    sanityCheck = true;
    testIndependentModules("var x = 10;", "var y = x++;",
                           null, null);
  }

// com.google.javascript.jscomp.VarCheckTest::testViolatedModuleDependencySkipNonStrict
  public void testViolatedModuleDependencySkipNonStrict() {
    sanityCheck = true;
    testDependentModules("var y = x++;", "var x = 10;",
                         null);
  }

// com.google.javascript.jscomp.VarCheckTest::testMissingModuleDependencySkipNonStrictNotPromoted
  public void testMissingModuleDependencySkipNonStrictNotPromoted() {
    sanityCheck = true;
    strictModuleDepErrorLevel = CheckLevel.ERROR;
    testIndependentModules("var x = 10;", "var y = x++;", null, null);
  }

// com.google.javascript.jscomp.VarCheckTest::testViolatedModuleDependencyNonStrictNotPromoted
  public void testViolatedModuleDependencyNonStrictNotPromoted() {
    sanityCheck = true;
    strictModuleDepErrorLevel = CheckLevel.ERROR;
    testDependentModules("var y = x++;", "var x = 10;", null);
  }

// com.google.javascript.jscomp.VarCheckTest::testDependentStrictModuleDependencyCheck
  public void testDependentStrictModuleDependencyCheck() {
    strictModuleDepErrorLevel = CheckLevel.ERROR;
    testDependentModules("var f = function() {return new B();};",
        "var B = function() {}",
        VarCheck.STRICT_MODULE_DEP_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testIndependentStrictModuleDependencyCheck
  public void testIndependentStrictModuleDependencyCheck() {
    strictModuleDepErrorLevel = CheckLevel.ERROR;
    testIndependentModules("var f = function() {return new B();};",
        "var B = function() {}",
        VarCheck.STRICT_MODULE_DEP_ERROR, null);
  }

// com.google.javascript.jscomp.VarCheckTest::testStarStrictModuleDependencyCheck
  public void testStarStrictModuleDependencyCheck() {
    strictModuleDepErrorLevel = CheckLevel.WARNING;
    testSame(createModuleStar("function a() {}", "function b() { a(); c(); }",
        "function c() { a(); }"),
        VarCheck.STRICT_MODULE_DEP_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testForwardVarReferenceInLocalScope1
  public void testForwardVarReferenceInLocalScope1() {
    testDependentModules("var x = 10; function a() {y++;}",
                         "var y = 11; a();", null);
  }

// com.google.javascript.jscomp.VarCheckTest::testForwardVarReferenceInLocalScope2
  public void testForwardVarReferenceInLocalScope2() {
    
    
    testDependentModules("var x = 10; function a() {y++;} a();",
                         "var y = 11;", null);
  }

// com.google.javascript.jscomp.VarCheckTest::testSimple
  public void testSimple() {
    checkSynthesizedExtern("x", "var x;");
    checkSynthesizedExtern("var x", "");
  }

// com.google.javascript.jscomp.VarCheckTest::testSimpleSanityCheck
  public void testSimpleSanityCheck() {
    sanityCheck = true;
    try {
      checkSynthesizedExtern("x", "");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().indexOf("Unexpected variable x") != -1);
    }
  }

// com.google.javascript.jscomp.VarCheckTest::testParameter
  public void testParameter() {
    checkSynthesizedExtern("function f(x){}", "");
  }

// com.google.javascript.jscomp.VarCheckTest::testLocalVar
  public void testLocalVar() {
    checkSynthesizedExtern("function f(){x}", "var x");
  }

// com.google.javascript.jscomp.VarCheckTest::testTwoLocalVars
  public void testTwoLocalVars() {
    checkSynthesizedExtern("function f(){x}function g() {x}", "var x");
  }

// com.google.javascript.jscomp.VarCheckTest::testInnerFunctionLocalVar
  public void testInnerFunctionLocalVar() {
    checkSynthesizedExtern("function f(){function g() {x}}", "var x");
  }

// com.google.javascript.jscomp.VarCheckTest::testNoCreateVarsForLabels
  public void testNoCreateVarsForLabels() {
    checkSynthesizedExtern("x:var y", "");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns1
  public void testVariableInNormalCodeUsedInExterns1() {
    checkSynthesizedExtern(
        "x.foo;", "var x;", "var x; x.foo;");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns2
  public void testVariableInNormalCodeUsedInExterns2() {
    checkSynthesizedExtern(
        "x;", "var x;", "var x; x;");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns3
  public void testVariableInNormalCodeUsedInExterns3() {
    checkSynthesizedExtern(
        "x.foo;", "function x() {}", "var x; x.foo; ");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns4
  public void testVariableInNormalCodeUsedInExterns4() {
    checkSynthesizedExtern(
        "x;", "function x() {}", "var x; x; ");
  }

// com.google.javascript.jscomp.VarCheckTest::testRedeclaration1
  public void testRedeclaration1() {
     String js = "var a; var a;";
     test(js, null, VarCheck.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testRedeclaration2
  public void testRedeclaration2() {
    String js = "var a;  var a;";
    testSame(js);
  }

// com.google.javascript.jscomp.VarCheckTest::testRedeclaration3
  public void testRedeclaration3() {
    String js = "  var a; var a; ";
    testSame(js);
  }

// com.google.javascript.jscomp.VarCheckTest::testDuplicateVar
  public void testDuplicateVar() {
    test(" var DEF = false; var DEF = true;",
         null, VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testFunctionScopeArguments
  public void testFunctionScopeArguments() {
    
    testSame("function f() {var arguments}");

    test("var f = function arguments() {}",
        null, VarCheck.VAR_ARGUMENTS_SHADOWED_ERROR);
    test("var f = function (arguments) {}",
        null, VarCheck.VAR_ARGUMENTS_SHADOWED_ERROR);
    test("function f() {try {} catch(arguments) {}}",
        null, VarCheck.VAR_ARGUMENTS_SHADOWED_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testNoUndeclaredVarWhenUsingClosurePass
  public void testNoUndeclaredVarWhenUsingClosurePass() {
    enableClosurePass();
    
    test("goog.require('namespace.Class1');\n", null,
        ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectCode
  public void testCorrectCode() {
    assertNoWarning("function foo(d) { (function() { d.foo(); }); d.bar(); } ");
    assertNoWarning("function foo() { bar(); } function bar() { foo(); } ");
    assertNoWarning("function f(d) { d = 3; }");
    assertNoWarning(VARIABLE_RUN);
    assertNoWarning("function f() { " + VARIABLE_RUN + "}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectShadowing
  public void testCorrectShadowing() {
    assertNoWarning(VARIABLE_RUN + "function f() { " + VARIABLE_RUN + "}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectRedeclare
  public void testCorrectRedeclare() {
    assertNoWarning(
        "function f() { if (1) { var a = 2; } else { var a = 3; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectRecursion
  public void testCorrectRecursion() {
    assertNoWarning("function f() { var x = function() { x(); }; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectCatch
  public void testCorrectCatch() {
    assertNoWarning("function f() { try { var x = 2; } catch (x) {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testRedeclare
  public void testRedeclare() {
    
    assertRedeclare("function f() { var a = 2; var a = 3; }");
    assertRedeclare("function f(a) { var a = 2; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testEarlyReference
  public void testEarlyReference() {
    assertUndeclared("function f() { a = 2; var a = 3; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectEarlyReference
  public void testCorrectEarlyReference() {
    assertNoWarning("var goog = goog || {}");
    assertNoWarning("function f() { a = 2; } var a = 2;");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testUnreferencedBleedingFunction
  public void testUnreferencedBleedingFunction() {
    assertNoWarning("var x = function y() {}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testReferencedBleedingFunction
  public void testReferencedBleedingFunction() {
    assertNoWarning("var x = function y() { return y(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testDoubleDeclaration
  public void testDoubleDeclaration() {
    assertRedeclare("function x(y) { if (true) { var y; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testDoubleDeclaration2
  public void testDoubleDeclaration2() {
    assertRedeclare("function x() { var y; if (true) { var y; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testHoistedFunction1
  public void testHoistedFunction1() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("f(); function f() {}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testHoistedFunction2
  public void testHoistedFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction
  public void testNonHoistedFunction() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("if (true) { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction2
  public void testNonHoistedFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("if (false) { function f() {} f(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction3
  public void testNonHoistedFunction3() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() {} f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction4
  public void testNonHoistedFunction4() {
    enableAmbiguousFunctionCheck = true;
    assertAmbiguous("if (false) { function f() {} }  f();");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction5
  public void testNonHoistedFunction5() {
    enableAmbiguousFunctionCheck = true;
    assertAmbiguous("function g() { if (false) { function f() {} }  f(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction6
  public void testNonHoistedFunction6() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("if (false) { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction7
  public void testNonHoistedFunction7() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("function g() { if (false) { f(); function f() {} }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction1
  public void testNonHoistedRecursiveFunction1() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("if (false) { function f() { f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction2
  public void testNonHoistedRecursiveFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() { f(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction3
  public void testNonHoistedRecursiveFunction3() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() { f(); g(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNoWarnInExterns1
  public void testNoWarnInExterns1() {
    
    String externs =
       "var google;" +
       " var google";
    String code = "";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNoWarnInExterns2
  public void testNoWarnInExterns2() {
    
    String externs =
       "window;" +
       "var window;";
    String code = "";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testStrictScript
  public void testStrictScript() throws Exception {
    assertNull(newParse("").getDirectives());
    assertEquals(
        Sets.newHashSet("use strict"),
        newParse("'use strict'").getDirectives());
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral2
  public void testArrayLiteral2() throws Exception {
    testNewParser("[a, , b]",
      "SCRIPT 1 [source_file: FileName.js] [length: 8]\n" +
      "    EXPR_RESULT 1 [source_file: FileName.js] [length: 8]\n" +
      "        ARRAYLIT 1 [source_file: FileName.js] [length: 8]\n" +
      "            NAME a 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            NAME b 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral4
  public void testArrayLiteral4() throws Exception {
    testNewParser("[,,,a,,b]",
      "SCRIPT 1 [source_file: FileName.js] [length: 9]\n" +
      "    EXPR_RESULT 1 [source_file: FileName.js] [length: 9]\n" +
      "        ARRAYLIT 1 [source_file: FileName.js] [length: 9]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            NAME a 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            NAME b 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral
  public void testObjectLiteral() {
    newParse("var o = {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral2
  public void testObjectLiteral2() {
    newParse("var o = {a: 1}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral3
  public void testObjectLiteral3() {
    newParse("var o = {a: 1, b: 2}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral4
  public void testObjectLiteral4() {
    newParse("var o = {1: 'a'}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral5
  public void testObjectLiteral5() {
    newParse("var o = {'a': 'a'}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral6
  public void testObjectLiteral6() {
    testNewParser("({1: true})",
      "SCRIPT 1 [source_file: FileName.js] [length: 11]\n" +
      "    EXPR_RESULT 1 [source_file: FileName.js] [length: 11]\n" +
      "        OBJECTLIT 1 [source_file: FileName.js] [length: 9]\n" +
      "            STRING_KEY 1 1 [quoted: 1] [source_file: FileName.js] [length: 1]\n" +
      "                TRUE 1 [source_file: FileName.js] [length: 4]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral7
  public void testObjectLiteral7() {
    mode = LanguageMode.ECMASCRIPT5;

    testNewParser("({get 1() {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 14]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 14]\n" +
        "        OBJECTLIT 1 [source_file: FileName.js] [length: 12]\n" +
        "            GETTER_DEF 1 1 [quoted: 1] [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 6]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral8
  public void testObjectLiteral8() {
    mode = LanguageMode.ECMASCRIPT5;

    testNewParser("({set 1(a) {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 15]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 15]\n" +
        "        OBJECTLIT 1 [source_file: FileName.js] [length: 13]\n" +
        "            SETTER_DEF 1 1 [quoted: 1] [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 7]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                        NAME a 1 [source_file: FileName.js] [length: 1]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel
  public void testLabel() {
    testNewParser("foo: bar",
        "SCRIPT 1 [source_file: FileName.js] [length: 8]\n" +
        "    LABEL 1 [source_file: FileName.js] [length: 4]\n" +
        "        LABEL_NAME foo 1 [source_file: FileName.js] [length: 4]\n" +
        "        EXPR_RESULT 1 [source_file: FileName.js] [length: 3]\n" +
        "            NAME bar 1 [source_file: FileName.js] [length: 3]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel2
  public void testLabel2() {
    testNewParser("l: while (f()) { if (g()) { continue l; } }",
        "SCRIPT 1 [source_file: FileName.js] [length: 43]\n" +
        "    LABEL 1 [source_file: FileName.js] [length: 2]\n" +
        "        LABEL_NAME l 1 [source_file: FileName.js] [length: 2]\n" +
        "        WHILE 1 [source_file: FileName.js] [length: 40]\n" +
        "            CALL 1 [source_file: FileName.js] [length: 3]\n" +
        "                NAME f 1 [source_file: FileName.js] [length: 1]\n" +
        "            BLOCK 1 [source_file: FileName.js] [length: 28]\n" +
        "                IF 1 [source_file: FileName.js] [length: 24]\n" +
        "                    CALL 1 [source_file: FileName.js] [length: 3]\n" +
        "                        NAME g 1 [source_file: FileName.js] [length: 1]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 15]\n" +
        "                        CONTINUE 1 [source_file: FileName.js] [length: 11]\n" +
        "                            LABEL_NAME l 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel3
  public void testLabel3() {
    testNewParser("Foo:Bar:X:{ break Bar; }",
        "SCRIPT 1 [source_file: FileName.js] [length: 24]\n" +
        "    LABEL 1 [source_file: FileName.js] [length: 4]\n" +
        "        LABEL_NAME Foo 1 [source_file: FileName.js] [length: 4]\n" +
        "        LABEL 1 [source_file: FileName.js] [length: 4]\n" +
        "            LABEL_NAME Bar 1 [source_file: FileName.js] [length: 4]\n" +
        "            LABEL 1 [source_file: FileName.js] [length: 2]\n" +
        "                LABEL_NAME X 1 [source_file: FileName.js] [length: 2]\n" +
        "                BLOCK 1 [source_file: FileName.js] [length: 14]\n" +
        "                    BREAK 1 [source_file: FileName.js] [length: 10]\n" +
        "                        LABEL_NAME Bar 1 [source_file: FileName.js] [length: 3]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation1
  public void testNegation1() {
    testNewParser("-a",
        "SCRIPT 1 [source_file: FileName.js] [length: 2]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 2]\n" +
        "        NEG 1 [source_file: FileName.js] [length: 2]\n" +
        "            NAME a 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation2
  public void testNegation2() {
    testNewParser("-2",
        "SCRIPT 1 [source_file: FileName.js] [length: 2]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 2]\n" +
        "        NUMBER -2.0 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation3
  public void testNegation3() {
    testNewParser("1 - -2",
        "SCRIPT 1 [source_file: FileName.js] [length: 6]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 6]\n" +
        "        SUB 1 [source_file: FileName.js] [length: 6]\n" +
        "            NUMBER 1.0 1 [source_file: FileName.js] [length: 1]\n" +
        "            NUMBER -2.0 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testGetter
  public void testGetter() {
    mode = LanguageMode.ECMASCRIPT5;
    testNewParser("({get a() {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 14]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 14]\n" +
        "        OBJECTLIT 1 [source_file: FileName.js] [length: 12]\n" +
        "            GETTER_DEF a 1 [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 6]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSetter
  public void testSetter() {
    mode = LanguageMode.ECMASCRIPT5;
    testNewParser("({set a(x) {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 15]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 15]\n" +
        "        OBJECTLIT 1 [source_file: FileName.js] [length: 13]\n" +
        "            SETTER_DEF a 1 [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 7]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                        NAME x 1 [source_file: FileName.js] [length: 1]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete1
  public void testDelete1() {
    testNoParseError("delete a.b;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete2
  public void testDelete2() {
    testNoParseError("delete a['b'];");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete3
  public void testDelete3() {
    
    
    testNoParseError("delete a;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete4
  public void testDelete4() {
    testParseError("delete 'x';",
        "Invalid delete operand. Only properties can be deleted.");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommentPositions1
  public void testCommentPositions1() {
    Node root = newParse("function a(x) {};" +
        "function b(x) {}");
    Node a = root.getFirstChild();
    Node b = root.getLastChild();
    assertMarkerPosition(a, 1, 4);
    assertMarkerPosition(b, 1, 45);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommentPositions2
  public void testCommentPositions2() {
    Node root = newParse(
        "\n" +
        "\n" +
        "function a(x) {};\n" +
        "\n" +
        "\n" +
        "\n" +
        "\n" +
        "function b(x) {};");
    assertMarkerPosition(root.getFirstChild(), 4, 4);
    assertMarkerPosition(root.getFirstChild().getNext().getNext(), 11, 6);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLiteralLocation
   public void testLiteralLocation() {
    Node root = newParse(
        "var d =\n" +
        "    \"foo\";\n" +
        "var e =\n" +
        "    1;\n" +
        "var f = \n" +
        "    1.2;\n" +
        "var g = \n" +
        "    2e5;\n" +
        "var h = \n" +
        "    'bar';\n");

    Node firstStmt = root.getFirstChild();
    Node firstLiteral = firstStmt.getFirstChild().getFirstChild();
    Node secondStmt = firstStmt.getNext();
    Node secondLiteral = secondStmt.getFirstChild().getFirstChild();
    Node thirdStmt = secondStmt.getNext();
    Node thirdLiteral = thirdStmt.getFirstChild().getFirstChild();
    Node fourthStmt = thirdStmt.getNext();
    Node fourthLiteral = fourthStmt.getFirstChild().getFirstChild();
    Node fifthStmt = fourthStmt.getNext();
    Node fifthLiteral = fifthStmt.getFirstChild().getFirstChild();

    assertNodePosition(2, 4, firstLiteral);
    assertNodePosition(4, 4, secondLiteral);
    assertNodePosition(6, 4, thirdLiteral);
    assertNodePosition(8, 4, fourthLiteral);
    assertNodePosition(10, 4, fifthLiteral);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSwitchLocation
  public void testSwitchLocation() {
    Node root = newParse(
        "switch (a) {\n" +
        "  
        "   case 1:\n" +
        "     b++;\n" +
        "   case 2:\n" +
        "   default:\n" +
        "     b--;\n" +
        "  }\n");

    Node switchStmt = root.getFirstChild();
    Node switchVar = switchStmt.getFirstChild();
    Node firstCase = switchVar.getNext();
    Node caseArg = firstCase.getFirstChild();
    Node caseBody = caseArg.getNext();
    Node caseExprStmt = caseBody.getFirstChild();
    Node incrExpr = caseExprStmt.getFirstChild();
    Node incrVar = incrExpr.getFirstChild();
    Node secondCase = firstCase.getNext();
    Node defaultCase = secondCase.getNext();

    assertNodePosition(1, 0, switchStmt);
    assertNodePosition(1, 8, switchVar);
    assertNodePosition(3, 3, firstCase);
    assertNodePosition(3, 8, caseArg);
    assertNodePosition(3, 3, caseBody);
    assertNodePosition(4, 5, caseExprStmt);
    assertNodePosition(4, 5, incrExpr);
    assertNodePosition(4, 5, incrVar);
    assertNodePosition(5, 3, secondCase);
    assertNodePosition(6, 3, defaultCase);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunctionParamLocation
  public void testFunctionParamLocation() {
    Node root = newParse(
        "function\n" +
        "     foo(a,\n" +
        "     b,\n" +
        "     c)\n" +
        "{}\n");

    Node function = root.getFirstChild();
    Node functionName = function.getFirstChild();
    Node params = functionName.getNext();
    Node param1 = params.getFirstChild();
    Node param2 = param1.getNext();
    Node param3 = param2.getNext();
    Node body = params.getNext();

    assertNodePosition(1, 0, function);
    assertNodePosition(2, 5, functionName);
    
    
    
    assertNodePosition(2, 8, params);
    assertNodePosition(2, 9, param1);
    assertNodePosition(3, 5, param2);
    assertNodePosition(4, 5, param3);
    assertNodePosition(5, 0, body);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testVarDeclLocation
  public void testVarDeclLocation() {
    Node root = newParse(
        "var\n" +
        "    a =\n" +
        "    3\n");
    Node varDecl = root.getFirstChild();
    Node varName = varDecl.getFirstChild();
    Node varExpr = varName.getFirstChild();

    assertNodePosition(1, 0, varDecl);
    assertNodePosition(2, 4, 1, varName);
    assertNodePosition(3, 4, 1, varExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturnLocation
  public void testReturnLocation() {
    Node root = newParse(
        "function\n" +
        "    foo(\n" +
        "    a,\n" +
        "    b,\n" +
        "    c) {\n" +
        "    return\n" +
        "    4;\n" +
        "}\n");

    Node function = root.getFirstChild();
    Node functionName = function.getFirstChild();
    Node params = functionName.getNext();
    Node body = params.getNext();
    Node returnStmt = body.getFirstChild();
    Node exprStmt = returnStmt.getNext();
    Node returnVal = exprStmt.getFirstChild();

    assertNodePosition(6, 4, returnStmt);
    assertNodePosition(7, 4, exprStmt);
    assertNodePosition(7, 4, returnVal);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLinenoFor
  public void testLinenoFor() {
    Node root = newParse(
        "for(\n" +
        ";\n" +
        ";\n" +
        ") {\n" +
        "}\n");

    Node forNode = root.getFirstChild();
    Node initClause= forNode.getFirstChild();
    Node condClause = initClause.getNext();
    Node incrClause = condClause.getNext();

    assertNodePosition(1, 0, forNode);
    assertNodePosition(2, 0, initClause);
    assertNodePosition(3, 0, condClause);
    
    
    
    
    
    assertNodePosition(-1, -1, incrClause); 
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBinaryExprLocation
  public void testBinaryExprLocation() {
    Node root = newParse(
        "var d = a\n" +
        "    + \n" +
        "    b;\n" +
        "var\n" +
        "    e =\n" +
        "    a +\n" +
        "    c;\n" +
        "var f = b\n" +
        "    / c;\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node firstVarAdd = firstVar.getFirstChild();

    Node secondVarDecl = firstVarDecl.getNext();
    Node secondVar = secondVarDecl.getFirstChild();
    Node secondVarAdd = secondVar.getFirstChild();

    Node thirdVarDecl = secondVarDecl.getNext();
    Node thirdVar = thirdVarDecl.getFirstChild();
    Node thirdVarAdd = thirdVar.getFirstChild();

    assertNodePosition(1, 0, firstVarDecl);
    assertNodePosition(1, 4, firstVar);
    assertNodePosition(1, 8, firstVarAdd);
    assertNodePosition(1, 8, firstVarAdd.getFirstChild());
    assertNodePosition(3, 4, firstVarAdd.getLastChild());

    assertNodePosition(4, 0, secondVarDecl);
    assertNodePosition(5, 4, secondVar);
    assertNodePosition(6, 4, secondVarAdd);
    assertNodePosition(6, 4, secondVarAdd.getFirstChild());
    assertNodePosition(7, 4, secondVarAdd.getLastChild());

    assertNodePosition(8, 0, thirdVarDecl);
    assertNodePosition(8, 4, thirdVar);
    assertNodePosition(8, 8, thirdVarAdd);
    assertNodePosition(8, 8, thirdVarAdd.getFirstChild());
    assertNodePosition(9, 6, thirdVarAdd.getLastChild());
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPrefixLocation
  public void testPrefixLocation() {
    Node root = newParse(
         "a++;\n" +
         "--\n" +
         "b;\n");

    Node firstStmt = root.getFirstChild();
    Node secondStmt = firstStmt.getNext();
    Node firstOp = firstStmt.getFirstChild();
    Node secondOp = secondStmt.getFirstChild();

    assertNodePosition(1, 0, firstOp);
    assertNodePosition(2, 0, secondOp);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testIfLocation
  public void testIfLocation() {
    Node root = newParse(
        "if\n" +
        "  (a == 3)\n" +
        "{\n" +
        "  b = 0;\n" +
        "}\n" +
        "  else\n" +
        "{\n" +
        "  c = 1;\n" +
        "}\n");

    Node ifStmt = root.getFirstChild();
    Node eqClause = ifStmt.getFirstChild();
    Node thenClause = eqClause.getNext();
    Node elseClause = thenClause.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(2, 3, eqClause);
    assertNodePosition(3, 0, thenClause);
    assertNodePosition(7, 0, elseClause);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryLocation
  public void testTryLocation() {
     Node root = newParse(
         "try {\n" +
         "  var x = 1;\n" +
         "} catch\n" +
         "   (err)\n" +
         "{\n" +
         "} finally {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node catchVarBlock = catchBlock.getFirstChild();
    Node catchVar = catchVarBlock.getFirstChild();
    Node finallyBlock = catchBlock.getNext();
    Node finallyStmt = finallyBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 2, catchVarBlock);
    assertNodePosition(4, 4, catchVar);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(6, 10, finallyBlock);
    assertNodePosition(7, 2, finallyStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testHookLocation
  public void testHookLocation() {
    Node root = newParse(
        "a\n" +
        "?\n" +
        "b\n" +
        ":\n" +
        "c\n" +
        ";\n");

    Node hookExpr = root.getFirstChild().getFirstChild();
    Node condExpr = hookExpr.getFirstChild();
    Node thenExpr = condExpr.getNext();
    Node elseExpr = thenExpr.getNext();

    assertNodePosition(2, 0, hookExpr);
    assertNodePosition(1, 0, condExpr);
    assertNodePosition(3, 0, thenExpr);
    assertNodePosition(5, 0, elseExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabelLocation
  public void testLabelLocation() {
    Node root = newParse(
        "foo:\n" +
        "a = 1;\n" +
        "bar:\n" +
        "b = 2;\n");

    Node firstStmt = root.getFirstChild();
    Node secondStmt = firstStmt.getNext();

    assertNodePosition(1, 0, firstStmt);
    assertNodePosition(3, 0, secondStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCompareLocation
  public void testCompareLocation() {
    Node root = newParse(
        "a\n" +
        "<\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(1, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
   }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testEqualityLocation
  public void testEqualityLocation() {
    Node root = newParse(
        "a\n" +
        "==\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(1, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPlusEqLocation
  public void testPlusEqLocation() {
    Node root = newParse(
        "a\n" +
        "+=\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(1, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommaLocation
  public void testCommaLocation() {
    Node root = newParse(
        "a,\n" +
        "b,\n" +
        "c;\n");

    Node statement = root.getFirstChild();
    Node comma1 = statement.getFirstChild();
    Node comma2 = comma1.getFirstChild();
    Node cRef = comma2.getNext();
    Node aRef = comma2.getFirstChild();
    Node bRef = aRef.getNext();

    assertNodePosition(1, 0, comma2);
    assertNodePosition(1, 0, aRef);
    assertNodePosition(2, 0, bRef);
    assertNodePosition(3, 0, cRef);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testRegexpLocation
  public void testRegexpLocation() {
    Node root = newParse(
        "var path =\n" +
        "replace(\n" +
        "/a/g," +
        "'/');\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node callNode = firstVar.getFirstChild();
    Node fnName = callNode.getFirstChild();
    Node regexObject = fnName.getNext();
    Node aString = regexObject.getFirstChild();
    Node endRegexString = regexObject.getNext();

    assertNodePosition(1, 0, firstVarDecl);
    assertNodePosition(1, 4, 4, firstVar);
    assertNodePosition(2, 0, 18, callNode);
    assertNodePosition(2, 0, 7, fnName);
    assertNodePosition(3, 0, regexObject);
    assertNodePosition(3, 0, aString);
    assertNodePosition(3, 5, endRegexString);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNestedOr
  public void testNestedOr() {
    Node root = newParse(
        "if (a && \n" +
        "    b() || \n" +
        "    \n" +
        "    c) {\n" +
        "}\n"
    );

    Node ifStmt = root.getFirstChild();
    Node orClause = ifStmt.getFirstChild();
    Node andClause = orClause.getFirstChild();
    Node cName = andClause.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(1, 4, orClause);
    assertNodePosition(1, 4, andClause);
    assertNodePosition(4, 4, cName);

  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBitwiseOps
  public void testBitwiseOps() {
      Node root = newParse(
        "if (a & \n" +
        "    b() | \n" +
        "    \n" +
        "    c) {\n" +
        "}\n"
    );

    Node ifStmt = root.getFirstChild();
    Node bitOr = ifStmt.getFirstChild();
    Node bitAnd = bitOr.getFirstChild();
    Node cName = bitAnd.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(1, 4, bitOr);
    assertNodePosition(1, 4, bitAnd);
    assertNodePosition(4, 4, cName);

  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLitLocation
  public void testObjectLitLocation() {
    Node root = newParse(
        "var foo =\n" +
        "{ \n" +
        "'A' : 'A', \n" +
        "'B' : 'B', \n" +
        "'C' :\n" +
        "    'C' \n" +
        "};\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node firstObjectLit = firstVar.getFirstChild();
    Node firstKey = firstObjectLit.getFirstChild();
    Node firstValue = firstKey.getFirstChild();

    Node secondKey = firstKey.getNext();
    Node secondValue = secondKey.getFirstChild();

    Node thirdKey = secondKey.getNext();
    Node thirdValue = thirdKey.getFirstChild();

    assertNodePosition(1, 4, firstVar);
    assertNodePosition(2, 0, firstObjectLit);

    assertNodePosition(3, 0, firstKey);
    assertNodePosition(3, 6, firstValue);

    assertNodePosition(4, 0, secondKey);
    assertNodePosition(4, 6, secondValue);

    assertNodePosition(5, 0, thirdKey);
    assertNodePosition(6, 4, thirdValue);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryWithoutCatchLocation
  public void testTryWithoutCatchLocation() {
     Node root = newParse(
         "try {\n" +
         "  var x = 1;\n" +
         "} finally {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node finallyBlock = catchBlock.getNext();
    Node finallyStmt = finallyBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(3, 10, finallyBlock);
    assertNodePosition(4, 2, finallyStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryWithoutFinallyLocation
  public void testTryWithoutFinallyLocation() {
     Node root = newParse(
         "try {\n" +
         "  var x = 1;\n" +
         "} catch (ex) {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node catchStmt = catchBlock.getFirstChild();
    Node exceptionVar = catchStmt.getFirstChild();
    Node exceptionBlock = exceptionVar.getNext();
    Node varDecl = exceptionBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(3, 2, catchStmt);
    assertNodePosition(3, 9, exceptionVar);
    assertNodePosition(3, 13, exceptionBlock);
    assertNodePosition(4, 2, varDecl);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testMultilineEqLocation
  public void testMultilineEqLocation() {
    Node  root = newParse(
        "if\n" +
        "    (((a == \n" +
        "  3) && \n" +
        "  (b == 2)) || \n" +
        " (c == 1)) {\n" +
        "}\n");
    Node ifStmt = root.getFirstChild();
    Node orTest = ifStmt.getFirstChild();
    Node andTest = orTest.getFirstChild();
    Node cTest = andTest.getNext();
    Node aTest = andTest.getFirstChild();
    Node bTest = aTest.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(2, 5, orTest);
    assertNodePosition(2, 6, andTest);
    assertNodePosition(2, 7, aTest);
    assertNodePosition(4, 3, bTest);
    assertNodePosition(5, 2, cTest);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testMultilineBitTestLocation
  public void testMultilineBitTestLocation() {
    Node root = newParse(
        "if (\n" +
        "      ((a \n" +
        "        | 3 \n" +
        "       ) == \n" +
        "       (b \n" +
        "        & 2)) && \n" +
        "      ((a \n" +
        "         ^ 0xffff) \n" +
        "       != \n" +
        "       (c \n" +
        "        << 1))) {\n" +
        "}\n");

    Node ifStmt = root.getFirstChild();
    Node andTest = ifStmt.getFirstChild();
    Node eqTest = andTest.getFirstChild();
    Node notEqTest = eqTest.getNext();

    Node bitOrTest = eqTest.getFirstChild();
    Node bitAndTest = bitOrTest.getNext();

    Node bitXorTest = notEqTest.getFirstChild();
    Node bitShiftTest = bitXorTest.getNext();

    assertNodePosition(1, 0, ifStmt);

    assertNodePosition(2, 7, eqTest);
    assertNodePosition(7, 7, notEqTest);

    assertNodePosition(2, 8, bitOrTest);
    assertNodePosition(5, 8, bitAndTest);
    assertNodePosition(7, 8, bitXorTest);
    assertNodePosition(10, 8, bitShiftTest);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCallLocation
  public void testCallLocation() {
    Node root = newParse(
        "a.\n" +
        "b.\n" +
        "cccc(1);\n");

    Node exprStmt = root.getFirstChild();
    Node functionCall = exprStmt.getFirstChild();
    Node functionProp = functionCall.getFirstChild();
    Node firstNameComponent = functionProp.getFirstChild();
    Node lastNameComponent = firstNameComponent.getNext();
    Node aNameComponent = firstNameComponent.getFirstChild();
    Node bNameComponent = aNameComponent.getNext();

    assertNodePosition(1, 0, 13, functionCall);
    assertNodePosition(1, 0, 10, functionProp);
    
    
    
    assertNodePosition(1, 0, 4, firstNameComponent);
    assertNodePosition(3, 0, 4, lastNameComponent);

    assertNodePosition(1, 0, 1, aNameComponent);
    assertNodePosition(2, 0, 1, bNameComponent);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNewLocation
  public void testNewLocation() {
    Node root = newParse(
        "new c();\n");

    Node exprStmt = root.getFirstChild();
    Node newExpr = exprStmt.getFirstChild();
    assertNodePosition(1, 0, 7, newExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNewLocationMultiLine
  public void testNewLocationMultiLine() {
    Node root = newParse(
        "new   \n" +
        "c();\n");

    Node exprStmt = root.getFirstChild();
    Node newExpr = exprStmt.getFirstChild();
    assertNodePosition(1, 0, 10, newExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLinenoDeclaration
  public void testLinenoDeclaration() {
    Node root = newParse(
        "a.\n" +
        "b=\n" +
        "function() {};\n");

    Node exprStmt = root.getFirstChild();
    Node fnAssignment =  exprStmt.getFirstChild();
    Node aDotbName = fnAssignment.getFirstChild();
    Node aName = aDotbName.getFirstChild();
    Node bName = aName.getNext();
    Node fnNode = aDotbName.getNext();
    Node fnName = fnNode.getFirstChild();

    assertNodePosition(1, 0, fnAssignment);
    
    
    assertNodePosition(1, 0, aName);
    assertNodePosition(2, 0, bName);
    assertNodePosition(3, 0, fnNode);
    assertNodePosition(3, 8, fnName);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testAssignmentValidation
  public void testAssignmentValidation() {
    testNoParseError("x=1");
    testNoParseError("x.y=1");
    testNoParseError("f().y=1");
    testParseError("(x||y)=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("(x?y:z)=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("f()=1", INVALID_ASSIGNMENT_TARGET);

    testNoParseError("x+=1");
    testNoParseError("x.y+=1");
    testNoParseError("f().y+=1");
    testParseError("(x||y)+=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("(x?y:z)+=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("f()+=1", INVALID_ASSIGNMENT_TARGET);

    testParseError("f()++", INVALID_INCREMENT_TARGET);
    testParseError("f()--", INVALID_DECREMENT_TARGET);
    testParseError("++f()", INVALID_INCREMENT_TARGET);
    testParseError("--f()", INVALID_DECREMENT_TARGET);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic1
  public void testParseTypeViaStatic1() throws Exception {
    Node typeNode = parseType("null");
    assertTypeEquals(NULL_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic2
  public void testParseTypeViaStatic2() throws Exception {
    Node typeNode = parseType("string");
    assertTypeEquals(STRING_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic3
  public void testParseTypeViaStatic3() throws Exception {
    Node typeNode = parseType("!Date");
    assertTypeEquals(DATE_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic4
  public void testParseTypeViaStatic4() throws Exception {
    Node typeNode = parseType("boolean|string");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, STRING_TYPE), typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInvalidTypeViaStatic
  public void testParseInvalidTypeViaStatic() throws Exception {
    Node typeNode = parseType("sometype.<anothertype");
    assertNull(typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInvalidTypeViaStatic2
  public void testParseInvalidTypeViaStatic2() throws Exception {
    Node typeNode = parseType("");
    assertNull(typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType1
  public void testParseNamedType1() throws Exception {
    assertNull(parse("@type null", "Unexpected end of file"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType2
  public void testParseNamedType2() throws Exception {
    JSDocInfo info = parse("@type null*/");
    assertTypeEquals(NULL_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType3
  public void testParseNamedType3() throws Exception {
    JSDocInfo info = parse("@type {string}*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType4
  public void testParseNamedType4() throws Exception {
    
    JSDocInfo info = parse("@type \n {string}*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType5
  public void testParseNamedType5() throws Exception {
    JSDocInfo info = parse("@type {!goog.\nBar}*/");
    assertTypeEquals(
        registry.createNamedType("goog.Bar", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType6
  public void testParseNamedType6() throws Exception {
    JSDocInfo info = parse("@type {!goog.\n * Bar.\n * Baz}*/");
    assertTypeEquals(
        registry.createNamedType("goog.Bar.Baz", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedTypeError1
  public void testParseNamedTypeError1() throws Exception {
    
    
    parse("@type {!goog\n * .Bar} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedTypeError2
  public void testParseNamedTypeError2() throws Exception {
    parse("@type {!goog.\n * Bar\n * .Baz} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamespaceType1
  public void testParseNamespaceType1() throws Exception {
    JSDocInfo info = parse("@type {goog.}*/");
    assertTypeEquals(
        registry.createNamedType("goog.", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType1
  public void testTypedefType1() throws Exception {
    JSDocInfo info = parse("@typedef string */");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(STRING_TYPE, info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType2
  public void testTypedefType2() throws Exception {
    JSDocInfo info = parse("@typedef \n {string}*/");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(STRING_TYPE, info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType3
  public void testTypedefType3() throws Exception {
    JSDocInfo info = parse("@typedef \n {(string|number)}*/");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(
        createUnionType(NUMBER_TYPE, STRING_TYPE),
        info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseStringType1
  public void testParseStringType1() throws Exception {
    assertTypeEquals(STRING_TYPE, parse("@type {string}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseStringType2
  public void testParseStringType2() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE, parse("@type {!String}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBooleanType1
  public void testParseBooleanType1() throws Exception {
    assertTypeEquals(BOOLEAN_TYPE, parse("@type {boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBooleanType2
  public void testParseBooleanType2() throws Exception {
    assertTypeEquals(
        BOOLEAN_OBJECT_TYPE, parse("@type {!Boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNumberType1
  public void testParseNumberType1() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@type {number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNumberType2
  public void testParseNumberType2() throws Exception {
    assertTypeEquals(NUMBER_OBJECT_TYPE, parse("@type {!Number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullType1
  public void testParseNullType1() throws Exception {
    assertTypeEquals(NULL_TYPE, parse("@type {null}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullType2
  public void testParseNullType2() throws Exception {
    assertTypeEquals(NULL_TYPE, parse("@type {Null}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseAllType1
  public void testParseAllType1() throws Exception {
    testParseType("*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseAllType2
  public void testParseAllType2() throws Exception {
    testParseType("*?", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseObjectType
  public void testParseObjectType() throws Exception {
    assertTypeEquals(OBJECT_TYPE, parse("@type {!Object}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDateType
  public void testParseDateType() throws Exception {
    assertTypeEquals(DATE_TYPE, parse("@type {!Date}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionType
  public void testParseFunctionType() throws Exception {
    assertTypeEquals(
        createNullableType(U2U_CONSTRUCTOR_TYPE),
        parse("@type {Function}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRegExpType
  public void testParseRegExpType() throws Exception {
    assertTypeEquals(REGEXP_TYPE, parse("@type {!RegExp}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseErrorTypes
  public void testParseErrorTypes() throws Exception {
    assertTypeEquals(ERROR_TYPE, parse("@type {!Error}*/").getType());
    assertTypeEquals(URI_ERROR_TYPE, parse("@type {!URIError}*/").getType());
    assertTypeEquals(EVAL_ERROR_TYPE, parse("@type {!EvalError}*/").getType());
    assertTypeEquals(REFERENCE_ERROR_TYPE,
        parse("@type {!ReferenceError}*/").getType());
    assertTypeEquals(TYPE_ERROR_TYPE, parse("@type {!TypeError}*/").getType());
    assertTypeEquals(
        RANGE_ERROR_TYPE, parse("@type {!RangeError}*/").getType());
    assertTypeEquals(
        SYNTAX_ERROR_TYPE, parse("@type {!SyntaxError}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType1
  public void testParseUndefinedType1() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {undefined}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType2
  public void testParseUndefinedType2() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {Undefined}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType3
  public void testParseUndefinedType3() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {void}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType1
  public void testParseTemplatizedType1() throws Exception {
    JSDocInfo info = parse("@type !Array.<number> */");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE, NUMBER_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType2
  public void testParseTemplatizedType2() throws Exception {
    JSDocInfo info = parse("@type {!Array.<number>}*/");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE, NUMBER_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType3
  public void testParseTemplatizedType3() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number,null)>*/");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE,
            createUnionType(NUMBER_TYPE, NULL_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType4
  public void testParseTemplatizedType4() throws Exception {
    JSDocInfo info = parse("@type {!Array.<(number|null)>}*/");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE,
            createUnionType(NUMBER_TYPE, NULL_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType5
  public void testParseTemplatizedType5() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Array.<(number|null)>>}*/");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE,
            createUnionType(NULL_TYPE,
                createTemplatizedType(ARRAY_TYPE,
                    createUnionType(NUMBER_TYPE, NULL_TYPE)))),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType6
  public void testParseTemplatizedType6() throws Exception {
    JSDocInfo info = parse("@type {!Array.<!Array.<(number|null)>>}*/");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE,
            createTemplatizedType(ARRAY_TYPE,
                createUnionType(NUMBER_TYPE, NULL_TYPE))),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType7
  public void testParseTemplatizedType7() throws Exception {
    JSDocInfo info = parse("@type {!Array.<function():Date>}*/");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE,
            registry.createFunctionType(
                createUnionType(DATE_TYPE, NULL_TYPE))),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType8
  public void testParseTemplatizedType8() throws Exception {
    JSDocInfo info = parse("@type {!Array.<function():!Date>}*/");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE,
            registry.createFunctionType(DATE_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType9
  public void testParseTemplatizedType9() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Date|number>}*/");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE,
            createUnionType(DATE_TYPE, NUMBER_TYPE, NULL_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType10
  public void testParseTemplatizedType10() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Date|number|boolean>}*/");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE,
            createUnionType(DATE_TYPE, NUMBER_TYPE, BOOLEAN_TYPE, NULL_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType11
  public void testParseTemplatizedType11() throws Exception {
    JSDocInfo info = parse("@type {!Object.<number>}*/");
    assertTypeEquals(
        createTemplatizedType(
            OBJECT_TYPE, ImmutableList.of(UNKNOWN_TYPE, NUMBER_TYPE)),
        info.getType());
    assertTemplatizedTypeEquals(
        registry.getObjectElementKey(), NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType12
  public void testParseTemplatizedType12() throws Exception {
    JSDocInfo info = parse("@type {!Object.<string,number>}*/");
    assertTypeEquals(
        createTemplatizedType(
            OBJECT_TYPE, ImmutableList.of(STRING_TYPE, NUMBER_TYPE)),
        info.getType());
    assertTemplatizedTypeEquals(
        registry.getObjectElementKey(), NUMBER_TYPE, info.getType());
    assertTemplatizedTypeEquals(
        registry.getObjectIndexKey(), STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTemplatizedType13
  public void testParseTemplatizedType13() throws Exception {
    JSDocInfo info = parse("@type !Array.<?> */");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE, UNKNOWN_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType1
  public void testParseUnionType1() throws Exception {
    JSDocInfo info = parse("@type {(boolean,null)}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType2
  public void testParseUnionType2() throws Exception {
    JSDocInfo info = parse("@type {boolean|null}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType3
  public void testParseUnionType3() throws Exception {
    JSDocInfo info = parse("@type {boolean||null}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType4
  public void testParseUnionType4() throws Exception {
    JSDocInfo info = parse("@type {(Array.<boolean>,null)}*/");
    assertTypeEquals(createUnionType(
        createTemplatizedType(
            ARRAY_TYPE, BOOLEAN_TYPE), NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType5
  public void testParseUnionType5() throws Exception {
    JSDocInfo info = parse("@type {(null, Array.<boolean>)}*/");
    assertTypeEquals(createUnionType(
        createTemplatizedType(
            ARRAY_TYPE, BOOLEAN_TYPE), NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType6
  public void testParseUnionType6() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>|null}*/");
    assertTypeEquals(createUnionType(
        createTemplatizedType(
            ARRAY_TYPE, BOOLEAN_TYPE), NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType7
  public void testParseUnionType7() throws Exception {
    JSDocInfo info = parse("@type {null|Array.<boolean>}*/");
    assertTypeEquals(createUnionType(
        createTemplatizedType(
            ARRAY_TYPE, BOOLEAN_TYPE), NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType8
  public void testParseUnionType8() throws Exception {
    JSDocInfo info = parse("@type {null||Array.<boolean>}*/");
    assertTypeEquals(createUnionType(
        createTemplatizedType(
            ARRAY_TYPE, BOOLEAN_TYPE), NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType9
  public void testParseUnionType9() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>||null}*/");
    assertTypeEquals(createUnionType(
        createTemplatizedType(
            ARRAY_TYPE, BOOLEAN_TYPE), NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType10
  public void testParseUnionType10() throws Exception {
    parse("@type {string|}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType11
  public void testParseUnionType11() throws Exception {
    parse("@type {(string,)}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType12
  public void testParseUnionType12() throws Exception {
    parse("@type {()}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType13
  public void testParseUnionType13() throws Exception {
    testParseType(
        "(function(this:Date),function(this:String):number)",
        "Function");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType14
  public void testParseUnionType14() throws Exception {
    testParseType(
        "(function(...[function(number):boolean]):number)|" +
        "function(this:String, string):number",
        "Function");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType15
  public void testParseUnionType15() throws Exception {
    testParseType("*|number", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType16
  public void testParseUnionType16() throws Exception {
    testParseType("number|*", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType17
  public void testParseUnionType17() throws Exception {
    testParseType("string|number|*", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType18
  public void testParseUnionType18() throws Exception {
    testParseType("(string,*,number)", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionTypeError1
  public void testParseUnionTypeError1() throws Exception {
    parse("@type {(string,|number)} */",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType1
  public void testParseUnknownType1() throws Exception {
    testParseType("?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType2
  public void testParseUnknownType2() throws Exception {
    testParseType("(?|number)", "?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType3
  public void testParseUnknownType3() throws Exception {
    testParseType("(number|?)", "?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType1
  public void testParseFunctionalType1() throws Exception {
    testParseType("function (): number");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType2
  public void testParseFunctionalType2() throws Exception {
    testParseType("function (number, string): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType3
  public void testParseFunctionalType3() throws Exception {
    testParseType(
        "function(this:Array)", "function (this:Array): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType4
  public void testParseFunctionalType4() throws Exception {
    testParseType("function (...[number]): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType5
  public void testParseFunctionalType5() throws Exception {
    testParseType("function (number, ...[string]): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType6
  public void testParseFunctionalType6() throws Exception {
    testParseType(
        "function (this:Date, number): (boolean|number|string)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType7
  public void testParseFunctionalType7() throws Exception {
    testParseType("function()", "function (): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType8
  public void testParseFunctionalType8() throws Exception {
    testParseType(
        "function(this:Array,...[boolean])",
        "function (this:Array, ...[boolean]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType9
  public void testParseFunctionalType9() throws Exception {
    testParseType(
        "function(this:Array,!Date,...[boolean?])",
        "function (this:Array, Date, ...[(boolean|null)]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType10
  public void testParseFunctionalType10() throws Exception {
    testParseType(
        "function(...[Object?]):boolean?",
        "function (...[(Object|null)]): (boolean|null)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType11
  public void testParseFunctionalType11() throws Exception {
    testParseType(
        "function(...[[number]]):[number?]",
        "function (...[Array]): Array");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType12
  public void testParseFunctionalType12() throws Exception {
    testParseType(
        "function(...)",
        "function (...[?]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType13
  public void testParseFunctionalType13() throws Exception {
    testParseType(
        "function(...): void",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType14
  public void testParseFunctionalType14() throws Exception {
    testParseType("function (*, string, number): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType15
  public void testParseFunctionalType15() throws Exception {
    testParseType("function (?, string): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType16
  public void testParseFunctionalType16() throws Exception {
    testParseType("function (string, ?): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType17
  public void testParseFunctionalType17() throws Exception {
    testParseType("(function (?): ?|number)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType18
  public void testParseFunctionalType18() throws Exception {
    testParseType("function (?): (?|number)", "function (?): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType19
  public void testParseFunctionalType19() throws Exception {
    testParseType(
        "function(...[?]): void",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testStructuralConstructor
  public void testStructuralConstructor() throws Exception {
    JSType type = testParseType(
        "function (new:Object)", "function (new:Object): ?");
    assertTrue(type.isConstructor());
    assertFalse(type.isNominalConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testStructuralConstructor2
  public void testStructuralConstructor2() throws Exception {
    JSType type = testParseType(
        "function (new:?)",
        
        "function (): ?");
    assertTrue(type.isConstructor());
    assertFalse(type.isNominalConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testStructuralConstructor3
  public void testStructuralConstructor3() throws Exception {
    resolve(parse("@type {function (new:*)} */").getType(),
        "constructed type must be an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testNominalConstructor
  public void testNominalConstructor() throws Exception {
    ObjectType type = testParseType("Array", "(Array|null)").dereference();
    assertTrue(type.getConstructor().isNominalConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug1419535
  public void testBug1419535() throws Exception {
    parse("@type {function(Object, string, *)?} */");
    parse("@type {function(Object, string, *)|null} */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIssue477
  public void testIssue477() throws Exception {
    parse("@type function */",
        "Bad type annotation. missing opening (");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testMalformedThisAnnotation
  public void testMalformedThisAnnotation() throws Exception {
    parse("@this */",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError1
  public void testParseFunctionalTypeError1() throws Exception {
    parse("@type {function number):string}*/",
        "Bad type annotation. missing opening (");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError2
  public void testParseFunctionalTypeError2() throws Exception {
    parse("@type {function( number}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError3
  public void testParseFunctionalTypeError3() throws Exception {
    parse("@type {function(...[number], string)}*/",
        "Bad type annotation. variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError4
  public void testParseFunctionalTypeError4() throws Exception {
    parse("@type {function(string, ...[number], boolean):string}*/",
        "Bad type annotation. variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError5
  public void testParseFunctionalTypeError5() throws Exception {
    parse("@type {function (thi:Array)}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError6
  public void testParseFunctionalTypeError6() throws Exception {
    resolve(parse("@type {function (this:number)}*/").getType(),
        "this type must be an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError7
  public void testParseFunctionalTypeError7() throws Exception {
    parse("@type {function(...[number)}*/",
        "Bad type annotation. missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError8
  public void testParseFunctionalTypeError8() throws Exception {
    parse("@type {function(...number])}*/",
        "Bad type annotation. missing opening [");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError9
  public void testParseFunctionalTypeError9() throws Exception {
    parse("@type {function (new:Array, this:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError10
  public void testParseFunctionalTypeError10() throws Exception {
    parse("@type {function (this:Array, new:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError11
  public void testParseFunctionalTypeError11() throws Exception {
    parse("@type {function (Array, new:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError12
  public void testParseFunctionalTypeError12() throws Exception {
    resolve(parse("@type {function (new:number)}*/").getType(),
        "constructed type must be an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayType1
  public void testParseArrayType1() throws Exception {
    testParseType("[number]", "Array");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayType2
  public void testParseArrayType2() throws Exception {
    testParseType("[(number,boolean,[Object?])]", "Array");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayType3
  public void testParseArrayType3() throws Exception {
    testParseType("[[number],[string]]?", "(Array|null)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError1
  public void testParseArrayTypeError1() throws Exception {
    parse("@type {[number}*/",
        "Bad type annotation. missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError2
  public void testParseArrayTypeError2() throws Exception {
    parse("@type {number]}*/",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError3
  public void testParseArrayTypeError3() throws Exception {
    parse("@type {[(number,boolean,Object?])]}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError4
  public void testParseArrayTypeError4() throws Exception {
    parse("@type {(number,boolean,[Object?)]}*/",
        "Bad type annotation. missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers1
  public void testParseNullableModifiers1() throws Exception {
    JSDocInfo info = parse("@type {string?}*/");
    assertTypeEquals(createNullableType(STRING_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers2
  public void testParseNullableModifiers2() throws Exception {
    JSDocInfo info = parse("@type {!Array.<string?>}*/");
    assertTypeEquals(
        createTemplatizedType(
            ARRAY_TYPE, createUnionType(STRING_TYPE, NULL_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers3
  public void testParseNullableModifiers3() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>?}*/");
    assertTypeEquals(
        createNullableType(createTemplatizedType(ARRAY_TYPE, BOOLEAN_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers4
  public void testParseNullableModifiers4() throws Exception {
    JSDocInfo info = parse("@type {(string,boolean)?}*/");
    assertTypeEquals(
        createNullableType(createUnionType(STRING_TYPE, BOOLEAN_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers5
  public void testParseNullableModifiers5() throws Exception {
    JSDocInfo info = parse("@type {(string?,boolean)}*/");
    assertTypeEquals(
        createUnionType(createNullableType(STRING_TYPE), BOOLEAN_TYPE),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers6
  public void testParseNullableModifiers6() throws Exception {
    JSDocInfo info = parse("@type {(string,boolean?)}*/");
    assertTypeEquals(
        createUnionType(STRING_TYPE, createNullableType(BOOLEAN_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers7
  public void testParseNullableModifiers7() throws Exception {
    JSDocInfo info = parse("@type {string?|boolean}*/");
    assertTypeEquals(
        createUnionType(createNullableType(STRING_TYPE), BOOLEAN_TYPE),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers8
  public void testParseNullableModifiers8() throws Exception {
    JSDocInfo info = parse("@type {string|boolean?}*/");
    assertTypeEquals(
        createUnionType(STRING_TYPE, createNullableType(BOOLEAN_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers9
  public void testParseNullableModifiers9() throws Exception {
    JSDocInfo info = parse("@type {foo.Hello.World?}*/");
    assertTypeEquals(
        createNullableType(
            registry.createNamedType(
                "foo.Hello.World", null, -1, -1)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseOptionalModifier
  public void testParseOptionalModifier() throws Exception {
    JSDocInfo info = parse("@type {function(number=)}*/");
    assertTypeEquals(
        registry.createFunctionType(
            UNKNOWN_TYPE, registry.createOptionalParameters(NUMBER_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline1
  public void testParseNewline1() throws Exception {
    JSDocInfo info = parse("@type {string\n* }\n*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline2
  public void testParseNewline2() throws Exception {
    JSDocInfo info = parse("@type !Array.<\n* number\n* > */");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE, NUMBER_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline3
  public void testParseNewline3() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number,\n* null)>*/");
    assertTypeEquals(
        createTemplatizedType(
            ARRAY_TYPE, createUnionType(NUMBER_TYPE, NULL_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline4
  public void testParseNewline4() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number|\n* null)>*/");
    assertTypeEquals(
        createTemplatizedType(
            ARRAY_TYPE, createUnionType(NUMBER_TYPE, NULL_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline5
  public void testParseNewline5() throws Exception {
    JSDocInfo info = parse("@type !Array.<function(\n* )\n* :\n* Date>*/");
    assertTypeEquals(
        createTemplatizedType(ARRAY_TYPE,
            registry.createFunctionType(
                createUnionType(DATE_TYPE, NULL_TYPE))),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseReturnType1
  public void testParseReturnType1() throws Exception {
    JSDocInfo info =
        parse("@return {null|string|Array.<boolean>}*/");
    assertTypeEquals(
        createUnionType(createTemplatizedType(ARRAY_TYPE, BOOLEAN_TYPE),
            NULL_TYPE, STRING_TYPE),
        info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseReturnType2
  public void testParseReturnType2() throws Exception {
    JSDocInfo info =
        parse("@returns {null|(string,Array.<boolean>)}*/");
    assertTypeEquals(
        createUnionType(createTemplatizedType(ARRAY_TYPE, BOOLEAN_TYPE),
            NULL_TYPE, STRING_TYPE),
        info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseReturnType3
  public void testParseReturnType3() throws Exception {
    JSDocInfo info =
        parse("@return {((null||Array.<boolean>,string),boolean)}*/");
    assertTypeEquals(
        createUnionType(createTemplatizedType(ARRAY_TYPE, BOOLEAN_TYPE),
            NULL_TYPE, STRING_TYPE, BOOLEAN_TYPE),
        info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType1
  public void testParseThisType1() throws Exception {
    JSDocInfo info =
        parse("@this {goog.foo.Bar}*/");
    assertTypeEquals(
        registry.createNamedType("goog.foo.Bar", null, -1, -1),
        info.getThisType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType2
  public void testParseThisType2() throws Exception {
    JSDocInfo info =
        parse("@this goog.foo.Bar*/");
    assertTypeEquals(
        registry.createNamedType("goog.foo.Bar", null, -1, -1),
        info.getThisType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType3
  public void testParseThisType3() throws Exception {
    parse("@type {number}\n@this goog.foo.Bar*/",
        "Bad type annotation. type annotation incompatible " +
        "with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType4
  public void testParseThisType4() throws Exception {
    resolve(parse("@this number*/").getThisType(),
        "@this must specify an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType5
  public void testParseThisType5() throws Exception {
    parse("@this {Date|Error}*/");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType6
  public void testParseThisType6() throws Exception {
    resolve(parse("@this {Date|number}*/").getThisType(),
        "@this must specify an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam1
  public void testParseParam1() throws Exception {
    JSDocInfo info = parse("@param {number} index*/");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam2
  public void testParseParam2() throws Exception {
    JSDocInfo info = parse("@param index*/");
    assertEquals(1, info.getParameterCount());
    assertEquals(null, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam3
  public void testParseParam3() throws Exception {
    JSDocInfo info = parse("@param {number} index useful comments*/");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam4
  public void testParseParam4() throws Exception {
    JSDocInfo info = parse("@param index useful comments*/");
    assertEquals(1, info.getParameterCount());
    assertEquals(null, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam5
  public void testParseParam5() throws Exception {
    
    JSDocInfo info = parse("@param {number} \n index */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam6
  public void testParseParam6() throws Exception {
    
    JSDocInfo info = parse("@param {number} \n * index */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam7
  public void testParseParam7() throws Exception {
    
    JSDocInfo info = parse("@param {number=} index */");
    assertTypeEquals(
        registry.createOptionalType(NUMBER_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam8
  public void testParseParam8() throws Exception {
    
    JSDocInfo info = parse("@param {...number} index */");
    assertTypeEquals(
        registry.createOptionalType(NUMBER_TYPE),
        info.getParameterType("index"));
  }
