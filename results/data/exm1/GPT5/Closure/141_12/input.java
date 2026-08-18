// buggy code
  static boolean canBeSideEffected(Node n, Set<String> knownConstants) {
    switch (n.getType()) {
      case Token.CALL:
      case Token.NEW:
        // Function calls or constructor can reference changed values.
        // TODO(johnlenz): Add some mechanism for determining that functions
        // are unaffected by side effects.
        return true;
      case Token.NAME:
        // Non-constant names values may have been changed.
        return !NodeUtil.isConstantName(n)
            && !knownConstants.contains(n.getString());

      // Properties on constant NAMEs can still be side-effected.
      case Token.GETPROP:
      case Token.GETELEM:
        return true;

        // Anonymous functions definitions are not changed by side-effects,
        // and named functions are not part of expressions.
    }

    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (canBeSideEffected(c, knownConstants)) {
        return true;
      }
    }

    return false;
  }

  private static Collection<Definition> getCallableDefinitions(
      DefinitionProvider definitionProvider, Node name) {
      List<Definition> result = Lists.newArrayList();

      if (!NodeUtil.isGetProp(name) && !NodeUtil.isName(name)) {
        return null;
      }
      Collection<Definition> decls =
          definitionProvider.getDefinitionsReferencedAt(name);
      if (decls == null) {
        return null;
      }

      for (Definition current : decls) {
        Node rValue = current.getRValue();
        if ((rValue != null) && NodeUtil.isFunction(rValue)) {
          result.add(current);
        } else {
          return null;
        }
      }

      return result;
  }

// relevant test
// com.google.javascript.jscomp.TypedScopeCreatorTest::testDuplicateExternProperty2
  public void testDuplicateExternProperty2() {
    testSame(
        " function Foo() {}" +
        " Foo.prototype.bar;" +
        "Foo.prototype.bar; var x = (new Foo).bar;", null);
    assertEquals("number", findNameType("x", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod
  public void testAbstractMethod() {
    testSame(
        " var abstractMethod;" +
        " function Foo() {}" +
        " Foo.prototype.bar = abstractMethod;");
    assertEquals(
        "Function", findNameType("abstractMethod", globalScope).toString());

    FunctionType ctor = (FunctionType) findNameType("Foo", globalScope);
    ObjectType instance = ctor.getInstanceType();
    assertEquals("Foo", instance.toString());

    ObjectType proto = instance.getImplicitPrototype();
    assertEquals("Foo.prototype", proto.toString());

    assertEquals(
        "function (this:Foo, number): ?",
        proto.getPropertyType("bar").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod2
  public void testAbstractMethod2() {
    testSame(
        " var abstractMethod;" +
        " var y = abstractMethod;");
    assertEquals(
        "Function",
        findNameType("y", globalScope).toString());
    assertEquals(
        "function (number): ?",
        globalScope.getVar("y").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod3
  public void testAbstractMethod3() {
    testSame(
        " var abstractMethod;" +
        " var y = abstractMethod; y;");
    assertEquals(
        "function (number): ?",
        findNameType("y", globalScope).toString());
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUnreachableCode
  public void testRemoveUnreachableCode() {
    
    test("function foo(){switch(foo){case 1:x=1;return;break;" +
         "case 2:{x=2;return;break}default:}}",
         "function foo(){switch(foo){case 1:x=1;return;" +
         "case 2:{x=2;return}default:}}");

    
    test("function bar(){if(foo)x=1;else if(bar){return;x=2}" +
         "else{x=3;return;x=4}return;x=5}",
         "function bar(){if(foo)x=1;else if(bar){return}" +
         "else{x=3;return}return}");

    
    test("function foo(){if(x==3)return;x=4;y++;while(y==4){return;x=3}}",
         "function foo(){if(x==3)return;x=4;y++;while(y==4){return}}");

    
    test("function baz(){for(i=0;i<n;i++){x=3;break;x=4}" +
         "do{x=2;break;x=4}while(x==4);" +
         "while(i<4){x=3;return;x=6}}",
         "function baz(){for(i=0;i<n;){x=3;break}" +
         "do{x=2;break}while(x==4);" +
         "while(i<4){x=3;return}}");

    
    test("function foo(){if(x==3){return}return;while(y==4){x++;return;x=4}}",
         "function foo(){if(x==3){return}return}");

    
    test("function foo(){return 3;for(;y==4;){x++;return;x=4}}",
         "function foo(){return 3}");

    
    test("function foo(){try{x=3;return x+1;x=5}catch(e){x=4;return;x=5}}",
         "function foo(){try{x=3;return x+1}catch(e){x=4;return}}");

    
    test("function foo(){try{x=3;return x+1;x=5}finally{x=4;return;x=5}}",
         "function foo(){try{x=3;return x+1}finally{x=4;return}}");

    
    test("function foo(){try{x=3;return x+1;x=5}catch(e){x=3;return;x=2}" +
         "finally{x=4;return;x=5}}",

         "function foo(){try{x=3;return x+1}catch(e){x=3;return}" +
         "finally{x=4;return}}");

    
    test("function foo(){x=3;if(x==4){x=5;return;x=6}else{x=7}return;x=3}",
         "function foo(){x=3;if(x==4){x=5;return}else{x=7}return}");

    
    test("function foo() { return 1; var x = 2; var y = 10; return 2;}",
         "function foo() { var y; var x; return 1}");

    test("function foo() { return 1; x = 2; y = 10; return 2;}",
         "function foo(){ return 1}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUselessNameStatements
  public void testRemoveUselessNameStatements() {
    test("a;", "");
    test("a.b;", "");
    test("a.b.MyClass.prototype.memberName;", "");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUselessStrings
  public void testRemoveUselessStrings() {
    test("'a';", "");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testNoRemoveUseStrict
  public void testNoRemoveUseStrict() {
    test("'use strict';", "'use strict'");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testNoRemoveUselessNameStatements
  public void testNoRemoveUselessNameStatements() {
    removeNoOpStatements = false;
    testSame("a;");
    testSame("a.b;");
    testSame("a.b.MyClass.prototype.memberName;");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveDo
  public void testRemoveDo() {
    test("do { print(1); break } while(1)", "do { print(1); break } while(1)");
    test("while(1) { break; do { print(1); break } while(1) }",
         "while(1) { break; do {} while(1) }");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUselessLiteralValueStatements
  public void testRemoveUselessLiteralValueStatements() {
    test("true;", "");
    test("'hi';", "");
    test("if (x) 1;", "");
    test("while (x) 1;", "while (x);");
    test("do 1; while (x);", "do ; while (x);");
    test("for (;;) 1;", "for (;;);");
    test("switch(x){case 1:true;case 2:'hi';default:true}",
         "switch(x){case 1:case 2:default:}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testConditionalDeadCode
  public void testConditionalDeadCode() {
    test("function f() { if (1) return; else return; x = 1}",
        "function f() { if (1) return; else return; }");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testSwitchCase
  public void testSwitchCase() {
    test("function f() { switch(x) { default: return; foo()}}",
         "function f() { switch(x) { default: return;}}");
    test("function f() { switch(x) { default: return; case 1: foo(); bar()}}",
         "function f() { switch(x) { default: return; case 1: foo(); bar()}}");
    test("function f() { switch(x) { default: return; case 1: return; bar()}}",
         "function f() { switch(x) { default: return; case 1: return;}}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testTryCatchFinally
  public void testTryCatchFinally() {
    testSame("try {foo()} catch (e) {bar()}");
    testSame("try { try {foo()} catch (e) {bar()}} catch (x) {bar()}");
    test("try {var x = 1} catch (e) {e()}", "{var x = 1}");
    test("try {var x = 1} catch (e) {e()} finally {x()}",
        " try {var x = 1}                 finally {x()}");
    test("try {var x = 1} catch (e) {e()} finally {}",
        "     {var x = 1}");
    testSame("try {var x = 1} finally {x()}");
    test("try {var x = 1} finally {}", "{var x = 1}");
    test("function f() { return; try{var x = 1}catch(e){} }",
        "function f() { var x; return; {}}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemovalRequiresRedeclaration
  public void testRemovalRequiresRedeclaration() {
    test("while(1) { break; var x = 1}", "var x; while(1) { break } ");
    test("while(1) { break; var x=1; var y=1}",
        "var y; var x; while(1) { break } ");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testAssignPropertyOnCreatedObject
  public void testAssignPropertyOnCreatedObject() {
    testSame("this.foo = 3;");
    testSame("a.foo = 3;");
    testSame("bar().foo = 3;");
    testSame("({}).foo = bar();");
    testSame("(new X()).foo = 3;");

    test("({}).foo = 3;", "");
    test("(function() {}).prototype.toString = function(){};", "");
    test("(function() {}).prototype['toString'] = function(){};", "");
    test("(function() {}).prototype[f] = function(){};", "");
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
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
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
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testVarReferenceInExterns
  public void testVarReferenceInExterns() {
    testSame("asdf;", "var asdf;", VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testCallInExterns
  public void testCallInExterns() {
    testSame("yz();", "function yz() {}",
             VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testVarInWithBlock
  public void testVarInWithBlock() {
    test("var a = {b:5}; with (a){b;}", null, VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testInvalidFunctionDecl1
  public void testInvalidFunctionDecl1() {
    test("function() {};", null, VarCheck.INVALID_FUNCTION_DECL);
  }

// com.google.javascript.jscomp.VarCheckTest::testInvalidFunctionDecl2
  public void testInvalidFunctionDecl2() {
    test("if (true) { function() {}; }", null, VarCheck.INVALID_FUNCTION_DECL);
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

// com.google.javascript.jscomp.VarCheckTest::testMissingModuleDependencySkipNonStrictPromoted
  public void testMissingModuleDependencySkipNonStrictPromoted() {
    sanityCheck = true;
    strictModuleDepErrorLevel = CheckLevel.ERROR;
    testIndependentModules("var x = 10;", "var y = x++;",
        VarCheck.STRICT_MODULE_DEP_ERROR, null);
  }

// com.google.javascript.jscomp.VarCheckTest::testViolatedModuleDependencyNonStrictPromoted
  public void testViolatedModuleDependencyNonStrictPromoted() {
    sanityCheck = true;
    strictModuleDepErrorLevel = CheckLevel.ERROR;
    testDependentModules("var y = x++;", "var x = 10;",
        VarCheck.STRICT_MODULE_DEP_ERROR);
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
    checkSynthesizedExtern("x", "var x");
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

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectCode
  public void testCorrectCode() {
    assertNoWarning("function foo(d) { (function() { d.foo(); }); d.bar(); } ");
    assertNoWarning("function foo() { bar(); } function bar() { foo(); } ");
    assertNoWarning("function(d) { d = 3; }");
    assertNoWarning(VARIABLE_RUN);
    assertNoWarning("function() { " + VARIABLE_RUN + "}");
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
    assertNoWarning("f(); function f() {}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testHoistedFunction2
  public void testHoistedFunction2() {
    assertNoWarning("function g() { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction
  public void testNonHoistedFunction() {
    assertUndeclared("if (true) { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction2
  public void testNonHoistedFunction2() {
    assertNoWarning("if (false) { function f() {} f(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction3
  public void testNonHoistedFunction3() {
    assertNoWarning("function g() { if (false) { function f() {} f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction4
  public void testNonHoistedFunction4() {
    if (VariableReferenceCheck.CHECK_UNHOISTED_NAMED_FUNCTIONS) {
      assertAmbiguous("if (false) { function f() {} }  f();");
    }
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction5
  public void testNonHoistedFunction5() {
    if (VariableReferenceCheck.CHECK_UNHOISTED_NAMED_FUNCTIONS) {
      assertAmbiguous("function g() { if (false) { function f() {} }  f(); }");
    }
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction6
  public void testNonHoistedFunction6() {
    assertUndeclared("if (false) { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction7
  public void testNonHoistedFunction7() {
    assertUndeclared("function g() { if (false) { f(); function f() {} }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction1
  public void testNonHoistedRecursiveFunction1() {
    assertNoWarning("if (false) { function f() { f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction2
  public void testNonHoistedRecursiveFunction2() {
    assertNoWarning("function g() { if (false) { function f() { f(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction3
  public void testNonHoistedRecursiveFunction3() {
    assertNoWarning("function g() { if (false) { function f() { f(); g(); }}}");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoWarnShadowGlobal
  public void testNoWarnShadowGlobal() {
    
    
    assertNoError("", "var x; function foo() { var x } ");
    assertNoError("var x", "function foo() { var x } ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testWarnShadowLocal1
  public void testWarnShadowLocal1() {
    assertError("", "function a(){ var x; function b() { var x = 1; } }");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testWarnShadowLocal2
  public void testWarnShadowLocal2() {
    assertError("",
                "function a(){" +
                "   var x;" +
                "  function b() {" +
                "    var x = 1;" +
                "  }" +
                "}");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testUseShadowGlobals1
  public void testUseShadowGlobals1() {
    assertNoError("", " var x; function foo() { x = 1 } ");
    assertNoError("", "function a() { var x; function b() { x = 1; } }");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotation
  public void testNoShadowAnnotation() {
    assertError("",
                " var x; function a() { var x } ");

    assertError("",
                " var x; function a() {function b(){var x}} ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotationInExterns1
  public void testNoShadowAnnotationInExterns1() {
    assertError(" var x",
                "function a() { var x } ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotationInExterns2
  public void testNoShadowAnnotationInExterns2() {
    assertError(" var x",
                "function a() {function b(){var x}} ");
  }
