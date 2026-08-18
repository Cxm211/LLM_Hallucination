// buggy code
  private void findDeclaredNames(Node n, Node parent, Renamer renamer) {
    // Do a shallow traversal, so don't traverse into function declarations,
    // except for the name of the function itself.
    if (parent == null
        || parent.getType() != Token.FUNCTION
        || n == parent.getFirstChild()) {
      if (NodeUtil.isVarDeclaration(n)) {
        renamer.addDeclaredName(n.getString());
      } else if (NodeUtil.isFunctionDeclaration(n)) {
        Node nameNode = n.getFirstChild();
        renamer.addDeclaredName(nameNode.getString());
      }

      for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
        findDeclaredNames(c, n, renamer);
      }
    }
  }

    private static String getOrginalNameInternal(String name, int index) {
      return name.substring(0, index);
    }

    private static String getNameSuffix(String name, int index) {
      return name.substring(
          index + ContextualRenamer.UNIQUE_ID_SEPARATOR.length(),
          name.length());
    }

    public void visit(NodeTraversal t, Node node, Node parent) {
      if (node.getType() == Token.NAME) {
        String oldName = node.getString();
        if (containsSeparator(oldName)) {
          Scope scope = t.getScope();
          Var var = t.getScope().getVar(oldName);
          if (var == null || var.isGlobal()) {
        return;
      }

          if (nameMap.containsKey(var)) {
            node.setString(nameMap.get(var));
          } else {
            int index = indexOfSeparator(oldName);
            String newName = getOrginalNameInternal(oldName, index);
            String suffix = getNameSuffix(oldName, index);

      // Merge any names that were referenced but not declared in the current
      // scope.
      // If there isn't anything left in the stack we will be going into the
      // global scope: don't try to build a set of referenced names for the
      // global scope.
            boolean recurseScopes = false;
            if (!suffix.matches("\\d+")) {
              recurseScopes = true;
            }

    /**
     * For the Var declared in the current scope determine if it is possible
     * to revert the name to its orginal form without conflicting with other
     * values.
     */
        // Check if the new name is valid and if it would cause conflicts.
            if (var.scope.isDeclared(newName, recurseScopes) ||
                !TokenStream.isJSIdentifier(newName)) {
              newName = oldName;
            } else {
              var.scope.declare(newName, var.nameNode, null, null);
          // Adding a reference to the new name to prevent either the parent
          // scopes or the current scope renaming another var to this new name.
              Node parentNode = var.getParentNode();
              if (parentNode.getType() == Token.FUNCTION &&
                  parentNode == var.scope.getRootNode()) {
                var.getNameNode().setString(newName);
              }
              node.setString(newName);
          compiler.reportCodeChange();
        }

            nameMap.put(var, newName);

      }

        // Add all referenced names to the set so it is possible to check for
        // conflicts.
        // Store only references to candidate names in the node map.
        }
      }
    }

    public void addDeclaredName(String name) {
        if (global) {
          reserveName(name);
        } else {
          // It hasn't been declared locally yet, so increment the count.
          if (!declarations.containsKey(name)) {
            int id = incrementNameCount(name);
            String newName = null;
            if (id != 0) {
              newName = getUniqueName(name, id);
          }
          declarations.put(name, newName);
        }
      }
    }

    public void addDeclaredName(String name) {
      if (!declarations.containsKey(name)) {
        declarations.put(name, getUniqueName(name));
      }
    }

  static boolean isSwitchCase(Node n) {
    return n.getType() == Token.CASE || n.getType() == Token.DEFAULT;
  }

  public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root,
        new NormalizeStatements(compiler, assertOnChange));
    removeDuplicateDeclarations(root);
    if (MAKE_LOCAL_NAMES_UNIQUE) {
      MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
      NodeTraversal t = new NodeTraversal(compiler, renamer);
      t.traverseRoots(externs, root);
    }
    // It is important that removeDuplicateDeclarations runs after
    // MakeDeclaredNamesUnique in order for catch block exception names to be
    // handled properly. Specifically, catch block exception names are
    // only valid within the catch block, but our currect Scope logic
    // has no concept of this and includes it in the containing function 
    // (or global scope). MakeDeclaredNamesUnique makes the catch exception
    // names unique so that removeDuplicateDeclarations() will properly handle
    // cases where a function scope variable conflict with a exception name:
    //   function f() {
    //      try {throw 0;} catch(e) {e; /* catch scope 'e'*/}
    //      var e = 1; // f scope 'e'
    //   }
    // otherwise 'var e = 1' would be rewritten as 'e = 1'.
    // TODO(johnlenz): Introduce a seperate scope for catch nodes. 
    new PropogateConstantAnnotations(compiler, assertOnChange)
        .process(externs, root);
  }

// relevant test
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
