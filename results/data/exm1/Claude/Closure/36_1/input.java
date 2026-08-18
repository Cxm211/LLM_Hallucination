// buggy code
    private boolean canInline(
        Reference declaration,
        Reference initialization,
        Reference reference) {
      if (!isValidDeclaration(declaration)
          || !isValidInitialization(initialization)
          || !isValidReference(reference)) {
        return false;
      }

      // If the value is read more than once, skip it.
      // VAR declarations and EXPR_RESULT don't need the value, but other
      // ASSIGN expressions parents do.
      if (declaration != initialization &&
          !initialization.getGrandparent().isExprResult()) {
        return false;
      }

      // Be very conservative and do no cross control structures or
      // scope boundaries
      if (declaration.getBasicBlock() != initialization.getBasicBlock()
          || declaration.getBasicBlock() != reference.getBasicBlock()) {
        return false;
      }

      // Do not inline into a call node. This would change
      // the context in which it was being called. For example,
      //   var a = b.c;
      //   a();
      // should not be inlined, because it calls a in the context of b
      // rather than the context of the window.
      //   var a = b.c;
      //   f(a)
      // is ok.
      Node value = initialization.getAssignedValue();
      Preconditions.checkState(value != null);
      if (value.isGetProp()
          && reference.getParent().isCall()
          && reference.getParent().getFirstChild() == reference.getNode()) {
        return false;
      }

      if (value.isFunction()) {
        Node callNode = reference.getParent();
        if (reference.getParent().isCall()) {
          CodingConvention convention = compiler.getCodingConvention();
          // Bug 2388531: Don't inline subclass definitions into class defining
          // calls as this confused class removing logic.
          SubclassRelationship relationship =
              convention.getClassesDefinedByCall(callNode);
          if (relationship != null) {
            return false;
          }

          // issue 668: Don't inline singleton getter methods
          // calls as this confused class removing logic.
        }
      }

      return canMoveAggressively(value) ||
          canMoveModerately(initialization, reference);
    }

// relevant test
// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst2
  public void testProvidedNamespaceIsConst2() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo.bar'); " +
         "function f() { foo.bar = {};}",
         "var foo$bar = {};" +
         "function f() { foo$bar = {}; }",
         ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst3
  public void testProvidedNamespaceIsConst3() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; " +
         "goog.provide('foo.bar'); goog.provide('foo.bar.baz'); " +
         " foo.bar = function() {};" +
         " foo.bar.baz = function() {};",
         "var foo$bar = function(){};" +
         "var foo$bar$baz = function(){};");
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst4
  public void testProvidedNamespaceIsConst4() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo.Bar'); " +
         "var foo = {}; foo.Bar = {};",
         "var foo = {}; var foo = {}; foo.Bar = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst5
  public void testProvidedNamespaceIsConst5() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo.Bar'); " +
         "foo = {}; foo.Bar = {};",
         "var foo = {}; foo = {}; foo.Bar = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testProcessDefinesAlwaysOn
  public void testProcessDefinesAlwaysOn() {
    test(createCompilerOptions(),
         " var HI = true; HI = false;",
         "var HI = false;false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testProcessDefinesAdditionalReplacements
  public void testProcessDefinesAdditionalReplacements() {
    CompilerOptions options = createCompilerOptions();
    options.setDefineToBooleanLiteral("HI", false);
    test(options,
         " var HI = true;",
         "var HI = false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testReplaceMessages
  public void testReplaceMessages() {
    CompilerOptions options = createCompilerOptions();
    String prefix = "var goog = {}; goog.getMsg = function() {};";
    testSame(options, prefix + "var MSG_HI = goog.getMsg('hi');");

    options.messageBundle = new EmptyMessageBundle();
    test(options,
        prefix + " var MSG_HI = goog.getMsg('hi');",
        prefix + "var MSG_HI = 'hi';");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckGlobalNames
  public void testCheckGlobalNames() {
    CompilerOptions options = createCompilerOptions();
    options.checkGlobalNamesLevel = CheckLevel.ERROR;
    test(options, "var x = {}; var y = x.z;",
         CheckGlobalNames.UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineGetters
  public void testInlineGetters() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "function Foo() {} Foo.prototype.bar = function() { return 3; };" +
        "var x = new Foo(); x.bar();";

    testSame(options, code);
    options.inlineGetters = true;

    test(options, code,
         "function Foo() {} Foo.prototype.bar = function() { return 3 };" +
         "var x = new Foo(); 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineGettersWithAmbiguate
  public void testInlineGettersWithAmbiguate() {
    CompilerOptions options = createCompilerOptions();

    String code =
        "" +
        "function Foo() {}" +
        " Foo.prototype.field;" +
        "Foo.prototype.getField = function() { return this.field; };" +
        "" +
        "function Bar() {}" +
        " Bar.prototype.field;" +
        "Bar.prototype.getField = function() { return this.field; };" +
        "new Foo().getField();" +
        "new Bar().getField();";

    testSame(options, code);

    options.inlineGetters = true;

    test(options, code,
        "function Foo() {}" +
        "Foo.prototype.field;" +
        "Foo.prototype.getField = function() { return this.field; };" +
        "function Bar() {}" +
        "Bar.prototype.field;" +
        "Bar.prototype.getField = function() { return this.field; };" +
        "new Foo().field;" +
        "new Bar().field;");

    options.checkTypes = true;
    options.ambiguateProperties = true;

    
    
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineVariables
  public void testInlineVariables() {
    CompilerOptions options = createCompilerOptions();
    String code = "function foo() {} var x = 3; foo(x);";
    testSame(options, code);

    options.inlineVariables = true;
    test(options, code, "(function foo() {})(3);");

    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    test(options, code, DefaultPassConfig.CANNOT_USE_PROTOTYPE_AND_VAR);
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineConstants
  public void testInlineConstants() {
    CompilerOptions options = createCompilerOptions();
    String code = "function foo() {} var x = 3; foo(x); var YYY = 4; foo(YYY);";
    testSame(options, code);

    options.inlineConstantVars = true;
    test(options, code, "function foo() {} var x = 3; foo(x); foo(4);");
  }

// com.google.javascript.jscomp.IntegrationTest::testMinimizeExits
  public void testMinimizeExits() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "function f() {" +
        "  if (window.foo) return; window.h(); " +
        "}";
    testSame(options, code);

    options.foldConstants = true;
    test(
        options, code,
        "function f() {" +
        "  window.foo || window.h(); " +
        "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldConstants
  public void testFoldConstants() {
    CompilerOptions options = createCompilerOptions();
    String code = "if (true) { window.foo(); }";
    testSame(options, code);

    options.foldConstants = true;
    test(options, code, "window.foo();");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnreachableCode
  public void testRemoveUnreachableCode() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { return; f(); }";
    testSame(options, code);

    options.removeDeadCode = true;
    test(options, code, "function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedPrototypeProperties1
  public void testRemoveUnusedPrototypeProperties1() {
    CompilerOptions options = createCompilerOptions();
    String code = "function Foo() {} " +
        "Foo.prototype.bar = function() { return new Foo(); };";
    testSame(options, code);

    options.removeUnusedPrototypeProperties = true;
    test(options, code, "function Foo() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedPrototypeProperties2
  public void testRemoveUnusedPrototypeProperties2() {
    CompilerOptions options = createCompilerOptions();
    String code = "function Foo() {} " +
        "Foo.prototype.bar = function() { return new Foo(); };" +
        "function f(x) { x.bar(); }";
    testSame(options, code);

    options.removeUnusedPrototypeProperties = true;
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testSmartNamePass
  public void testSmartNamePass() {
    CompilerOptions options = createCompilerOptions();
    String code = "function Foo() { this.bar(); } " +
        "Foo.prototype.bar = function() { return Foo(); };";
    testSame(options, code);

    options.smartNameRemoval = true;
    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testDeadAssignmentsElimination
  public void testDeadAssignmentsElimination() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { var x = 3; 4; x = 5; return x; } f(); ";
    testSame(options, code);

    options.deadAssignmentElimination = true;
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "function f() { var x = 3; 4; x = 5; return x; } f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineFunctions
  public void testInlineFunctions() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { return 3; } f(); ";
    testSame(options, code);

    options.inlineFunctions = true;
    test(options, code, "3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedVars1
  public void testRemoveUnusedVars1() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f(x) {} f();";
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "function f() {} f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedVars2
  public void testRemoveUnusedVars2() {
    CompilerOptions options = createCompilerOptions();
    String code = "(function f(x) {})();var g = function() {}; g();";
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "(function() {})();var g = function() {}; g();");

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.UNMAPPED;
    test(options, code, "(function f() {})();var g = function $g$() {}; g();");
  }

// com.google.javascript.jscomp.IntegrationTest::testCrossModuleCodeMotion
  public void testCrossModuleCodeMotion() {
    CompilerOptions options = createCompilerOptions();
    String[] code = new String[] {
      "var x = 1;",
      "x;",
    };
    testSame(options, code);

    options.crossModuleCodeMotion = true;
    test(options, code, new String[] {
      "",
      "var x = 1; x;",
    });
  }

// com.google.javascript.jscomp.IntegrationTest::testCrossModuleMethodMotion
  public void testCrossModuleMethodMotion() {
    CompilerOptions options = createCompilerOptions();
    String[] code = new String[] {
      "var Foo = function() {}; Foo.prototype.bar = function() {};" +
      "var x = new Foo();",
      "x.bar();",
    };
    testSame(options, code);

    options.crossModuleMethodMotion = true;
    test(options, code, new String[] {
      CrossModuleMethodMotion.STUB_DECLARATIONS +
      "var Foo = function() {};" +
      "Foo.prototype.bar=JSCompiler_stubMethod(0); var x=new Foo;",
      "Foo.prototype.bar=JSCompiler_unstubMethod(0,function(){}); x.bar()",
    });
  }

// com.google.javascript.jscomp.IntegrationTest::testFlowSensitiveInlineVariables1
  public void testFlowSensitiveInlineVariables1() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { var x = 3; x = 5; return x; }";
    testSame(options, code);

    options.flowSensitiveInlineVariables = true;
    test(options, code, "function f() { var x = 3; return 5; }");

    String unusedVar = "function f() { var x; x = 5; return x; } f()";
    test(options, unusedVar, "function f() { var x; return 5; } f()");

    options.removeUnusedVars = true;
    test(options, unusedVar, "function f() { return 5; } f()");
  }

// com.google.javascript.jscomp.IntegrationTest::testFlowSensitiveInlineVariables2
  public void testFlowSensitiveInlineVariables2() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    test(options,
        "function f () {\n" +
        "    var ab = 0;\n" +
        "    ab += '-';\n" +
        "    alert(ab);\n" +
        "}",
        "function f () {\n" +
        "    alert('0-');\n" +
        "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseAnonymousFunctions
  public void testCollapseAnonymousFunctions() {
    CompilerOptions options = createCompilerOptions();
    String code = "var f = function() {};";
    testSame(options, code);

    options.collapseAnonymousFunctions = true;
    test(options, code, "function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testMoveFunctionDeclarations
  public void testMoveFunctionDeclarations() {
    CompilerOptions options = createCompilerOptions();
    String code = "var x = f(); function f() { return 3; }";
    testSame(options, code);

    options.moveFunctionDeclarations = true;
    test(options, code, "function f() { return 3; } var x = f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testNameAnonymousFunctions
  public void testNameAnonymousFunctions() {
    CompilerOptions options = createCompilerOptions();
    String code = "var f = function() {};";
    testSame(options, code);

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.MAPPED;
    test(options, code, "var f = function $() {}");
    assertNotNull(lastCompiler.getResult().namedAnonFunctionMap);

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.UNMAPPED;
    test(options, code, "var f = function $f$() {}");
    assertNull(lastCompiler.getResult().namedAnonFunctionMap);
  }

// com.google.javascript.jscomp.IntegrationTest::testExtractPrototypeMemberDeclarations
  public void testExtractPrototypeMemberDeclarations() {
    CompilerOptions options = createCompilerOptions();
    String code = "var f = function() {};";
    String expected = "var a; var b = function() {}; a = b.prototype;";
    for (int i = 0; i < 10; i++) {
      code += "f.prototype.a = " + i + ";";
      expected += "a.a = " + i + ";";
    }
    testSame(options, code);

    options.extractPrototypeMemberDeclarations = true;
    options.variableRenaming = VariableRenamingPolicy.ALL;
    test(options, code, expected);

    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    options.variableRenaming = VariableRenamingPolicy.OFF;
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testDevirtualizationAndExtractPrototypeMemberDeclarations
  public void testDevirtualizationAndExtractPrototypeMemberDeclarations() {
    CompilerOptions options = createCompilerOptions();
    options.devirtualizePrototypeMethods = true;
    options.collapseAnonymousFunctions = true;
    options.extractPrototypeMemberDeclarations = true;
    options.variableRenaming = VariableRenamingPolicy.ALL;
    String code = "var f = function() {};";
    String expected = "var a; function b() {} a = b.prototype;";
    for (int i = 0; i < 10; i++) {
      code += "f.prototype.argz = function() {arguments};";
      code += "f.prototype.devir" + i + " = function() {};";

      char letter = (char) ('d' + i);
      expected += "a.argz = function() {arguments};";
      expected += "function " + letter + "(c){}";
    }

    code += "var F = new f(); F.argz();";
    expected += "var n = new b(); n.argz();";

    for (int i = 0; i < 10; i++) {
      code += "F.devir" + i + "();";

      char letter = (char) ('d' + i);
      expected += letter + "(n);";
    }
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testCoalesceVariableNames
  public void testCoalesceVariableNames() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() {var x = 3; var y = x; var z = y; return z;}";
    testSame(options, code);

    options.coalesceVariableNames = true;
    test(options, code,
         "function f() {var x = 3; x = x; x = x; return x;}");
  }

// com.google.javascript.jscomp.IntegrationTest::testPropertyRenaming
  public void testPropertyRenaming() {
    CompilerOptions options = createCompilerOptions();
    options.propertyAffinity = true;
    String code =
        "function f() { return this.foo + this['bar'] + this.Baz; }" +
        "f.prototype.bar = 3; f.prototype.Baz = 3;";
    String heuristic =
        "function f() { return this.foo + this['bar'] + this.a; }" +
        "f.prototype.bar = 3; f.prototype.a = 3;";
    String aggHeuristic =
        "function f() { return this.foo + this['b'] + this.a; } " +
        "f.prototype.b = 3; f.prototype.a = 3;";
    String all =
        "function f() { return this.b + this['bar'] + this.a; }" +
        "f.prototype.c = 3; f.prototype.a = 3;";
    testSame(options, code);

    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    test(options, code, heuristic);

    options.propertyRenaming = PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC;
    test(options, code, aggHeuristic);

    options.propertyRenaming = PropertyRenamingPolicy.ALL_UNQUOTED;
    test(options, code, all);
  }

// com.google.javascript.jscomp.IntegrationTest::testConvertToDottedProperties
  public void testConvertToDottedProperties() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "function f() { return this['bar']; } f.prototype.bar = 3;";
    String expected =
        "function f() { return this.bar; } f.prototype.a = 3;";
    testSame(options, code);

    options.convertToDottedProperties = true;
    options.propertyRenaming = PropertyRenamingPolicy.ALL_UNQUOTED;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testRewriteFunctionExpressions
  public void testRewriteFunctionExpressions() {
    CompilerOptions options = createCompilerOptions();
    String code = "var a = function() {};";
    String expected = "function JSCompiler_emptyFn(){return function(){}} " +
        "var a = JSCompiler_emptyFn();";
    for (int i = 0; i < 10; i++) {
      code += "a = function() {};";
      expected += "a = JSCompiler_emptyFn();";
    }
    testSame(options, code);

    options.rewriteFunctionExpressions = true;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testAliasAllStrings
  public void testAliasAllStrings() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { return 'a'; }";
    String expected = "var $$S_a = 'a'; function f() { return $$S_a; }";
    testSame(options, code);

    options.aliasAllStrings = true;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testAliasExterns
  public void testAliasExterns() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { return window + window + window + window; }";
    String expected = "var GLOBAL_window = window;" +
        "function f() { return GLOBAL_window + GLOBAL_window + " +
        "               GLOBAL_window + GLOBAL_window; }";
    testSame(options, code);

    options.aliasExternals = true;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testAliasKeywords
  public void testAliasKeywords() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "function f() { return true + true + true + true + true + true; }";
    String expected = "var JSCompiler_alias_TRUE = true;" +
        "function f() { return JSCompiler_alias_TRUE + " +
        "    JSCompiler_alias_TRUE + JSCompiler_alias_TRUE + " +
        "    JSCompiler_alias_TRUE + JSCompiler_alias_TRUE + " +
        "    JSCompiler_alias_TRUE; }";
    testSame(options, code);

    options.aliasKeywords = true;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testRenameVars1
  public void testRenameVars1() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "var abc = 3; function f() { var xyz = 5; return abc + xyz; }";
    String local = "var abc = 3; function f() { var a = 5; return abc + a; }";
    String all = "var a = 3; function c() { var b = 5; return a + b; }";
    testSame(options, code);

    options.variableRenaming = VariableRenamingPolicy.LOCAL;
    test(options, code, local);

    options.variableRenaming = VariableRenamingPolicy.ALL;
    test(options, code, all);

    options.reserveRawExports = true;
  }

// com.google.javascript.jscomp.IntegrationTest::testRenameVars2
  public void testRenameVars2() {
    CompilerOptions options = createCompilerOptions();
    options.variableRenaming = VariableRenamingPolicy.ALL;

    String code =     "var abc = 3; function f() { window['a'] = 5; }";
    String noexport = "var a = 3;   function b() { window['a'] = 5; }";
    String export =   "var b = 3;   function c() { window['a'] = 5; }";

    options.reserveRawExports = false;
    test(options, code, noexport);

    options.reserveRawExports = true;
    test(options, code, export);
  }

// com.google.javascript.jscomp.IntegrationTest::testShadowVaribles
  public void testShadowVaribles() {
    CompilerOptions options = createCompilerOptions();
    options.variableRenaming = VariableRenamingPolicy.LOCAL;
    options.shadowVariables = true;
    String code =     "var f = function(x) { return function(y) {}}";
    String expected = "var f = function(a) { return function(a) {}}";
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testRenameLabels
  public void testRenameLabels() {
    CompilerOptions options = createCompilerOptions();
    String code = "longLabel: while (true) { break longLabel; }";
    String expected = "a: while (true) { break a; }";
    testSame(options, code);

    options.labelRenaming = true;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testBadBreakStatementInIdeMode
  public void testBadBreakStatementInIdeMode() {
    
    
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    options.checkTypes = true;
    test(options,
         "function f() { try { } catch(e) { break; } }",
         RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue63SourceMap
  public void testIssue63SourceMap() {
    CompilerOptions options = createCompilerOptions();
    String code = "var a;";

    options.skipAllPasses = true;
    options.sourceMapOutputPath = "./src.map";

    Compiler compiler = compile(options, code);
    compiler.toSource();
  }

// com.google.javascript.jscomp.IntegrationTest::testRegExp1
  public void testRegExp1() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;

    String code = "/(a)/.test(\"a\");";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    String expected = "";

    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testRegExp2
  public void testRegExp2() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = "/(a)/.test(\"a\");var a = RegExp.$1";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    test(options, code, CheckRegExp.REGEXP_REFERENCE);

    options.setWarningLevel(DiagnosticGroups.CHECK_REGEXP, CheckLevel.OFF);

    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals1
  public void testFoldLocals1() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    
    
    String code = "new Widget().go();";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals2
  public void testFoldLocals2() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;
    options.checkTypes = true;

    
    
    String code = "widgetToken().go();";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    test(options, code, "widgetToken()");
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals3
  public void testFoldLocals3() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    
    
    String definition = "function f(){return new Widget()}";
    String call = "f().go();";
    String code = definition + call;

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    
    
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals4
  public void testFoldLocals4() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = "\n"
        + "function InternalWidget(){this.x = 1;}"
        + "InternalWidget.prototype.internalGo = function (){this.x = 2};"
        + "new InternalWidget().internalGo();";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    String optimized = ""
      + "function InternalWidget(){this.x = 1;}"
      + "InternalWidget.prototype.internalGo = function (){this.x = 2};";

    test(options, code, optimized);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals5
  public void testFoldLocals5() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = ""
        + "function fn(){var a={};a.x={};return a}"
        + "fn().x.y = 1;";

    
    
    String result = ""
        + "function fn(){var a={x:{}};return a}"
        + "fn().x.y = 1;";

    test(options, code, result);

    options.computeFunctionSideEffects = true;

    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals6
  public void testFoldLocals6() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = ""
        + "function fn(){return {}}"
        + "fn().x.y = 1;";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals7
  public void testFoldLocals7() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = ""
        + "function InternalWidget(){return [];}"
        + "Array.prototype.internalGo = function (){this.x = 2};"
        + "InternalWidget().internalGo();";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    String optimized = ""
      + "function InternalWidget(){return [];}"
      + "Array.prototype.internalGo = function (){this.x = 2};";

    test(options, code, optimized);
  }

// com.google.javascript.jscomp.IntegrationTest::testVarDeclarationsIntoFor
  public void testVarDeclarationsIntoFor() {
    CompilerOptions options = createCompilerOptions();

    options.collapseVariableDeclarations = false;

    String code = "var a = 1; for (var b = 2; ;) {}";

    testSame(options, code);

    options.collapseVariableDeclarations = false;

    test(options, code, "for (var a = 1, b = 2; ;) {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testExploitAssigns
  public void testExploitAssigns() {
    CompilerOptions options = createCompilerOptions();

    options.collapseVariableDeclarations = false;

    String code = "a = 1; b = a; c = b";

    testSame(options, code);

    options.collapseVariableDeclarations = true;

    test(options, code, "c=b=a=1");
  }

// com.google.javascript.jscomp.IntegrationTest::testRecoverOnBadExterns
  public void testRecoverOnBadExterns() throws Exception {
    
    
    
    
    
    
    
    
    
    CompilerOptions options = createCompilerOptions();

    options.aliasExternals = true;
    externs = new JSSourceFile[] {
      JSSourceFile.fromCode("externs", "extern.foo")
    };

    test(options,
         "var extern; " +
         "function f() { return extern + extern + extern + extern; }",
         "var extern; " +
         "function f() { return extern + extern + extern + extern; }",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testDuplicateVariablesInExterns
  public void testDuplicateVariablesInExterns() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = true;
    externs = new JSSourceFile[] {
      JSSourceFile.fromCode("externs",
          "var externs = {};  var externs = {};")
    };
    testSame(options, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testLanguageMode
  public void testLanguageMode() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);

    String code = "var a = {get f(){}}";

    Compiler compiler = compile(options, code);
    checkUnexpectedErrorsOrWarnings(compiler, 1);
    assertEquals(
        "JSC_PARSE_ERROR. Parse error. " +
        "getters are not supported in Internet Explorer " +
        "at i0 line 1 : 0",
        compiler.getErrors()[0].toString());

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);

    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testLanguageMode2
  public void testLanguageMode2() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    options.setWarningLevel(DiagnosticGroups.ES5_STRICT, CheckLevel.OFF);

    String code = "var a  = 2; delete a;";

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);

    test(options,
        code,
        code,
        StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue598
  public void testIssue598() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    WarningLevel.VERBOSE.setOptionsForWarningLevel(options);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    String code =
        "'use strict';\n" +
        "function App() {}\n" +
        "App.prototype = {\n" +
        "  get appData() { return this.appData_; },\n" +
        "  set appData(data) { this.appData_ = data; }\n" +
        "};";

    Compiler compiler = compile(options, code);
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testCoaleseVariables
  public void testCoaleseVariables() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = false;
    options.coalesceVariableNames = true;

    String code =
        "function f(a) {" +
        "  if (a) {" +
        "    return a;" +
        "  } else {" +
        "    var b = a;" +
        "    return b;" +
        "  }" +
        "  return a;" +
        "}";
    String expected =
        "function f(a) {" +
        "  if (a) {" +
        "    return a;" +
        "  } else {" +
        "    a = a;" +
        "    return a;" +
        "  }" +
        "  return a;" +
        "}";

    test(options, code, expected);

    options.foldConstants = true;
    options.coalesceVariableNames = false;

    code =
        "function f(a) {" +
        "  if (a) {" +
        "    return a;" +
        "  } else {" +
        "    var b = a;" +
        "    return b;" +
        "  }" +
        "  return a;" +
        "}";
    expected =
        "function f(a) {" +
        "  if (!a) {" +
        "    var b = a;" +
        "    return b;" +
        "  }" +
        "  return a;" +
        "}";

    test(options, code, expected);

    options.foldConstants = true;
    options.coalesceVariableNames = true;

    expected =
      "function f(a) {" +
      "  return a;" +
      "}";

    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testLateStatementFusion
  public void testLateStatementFusion() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    test(options,
        "while(a){a();if(b){b();b()}}",
        "for(;a;)a(),b&&(b(),b())");
  }

// com.google.javascript.jscomp.IntegrationTest::testLateConstantReordering
  public void testLateConstantReordering() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    test(options,
        "if (x < 1 || x > 1 || 1 < x || 1 > x) { alert(x) }",
        "   (1 > x || 1 < x || 1 < x || 1 > x) && alert(x) ");
  }

// com.google.javascript.jscomp.IntegrationTest::testsyntheticBlockOnDeadAssignments
  public void testsyntheticBlockOnDeadAssignments() {
    CompilerOptions options = createCompilerOptions();
    options.deadAssignmentElimination = true;
    options.removeUnusedVars = true;
    options.syntheticBlockStartMarker = "START";
    options.syntheticBlockEndMarker = "END";
    test(options, "var x; x = 1; START(); x = 1;END();x()",
                  "var x; x = 1;{START();{x = 1}END()}x()");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug4152835
  public void testBug4152835() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    options.syntheticBlockStartMarker = "START";
    options.syntheticBlockEndMarker = "END";
    test(options, "START();END()", "{START();{}END()}");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug5786871
  public void testBug5786871() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    test(options, "function () {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue378
  public void testIssue378() {
    CompilerOptions options = createCompilerOptions();
    options.inlineVariables = true;
    options.flowSensitiveInlineVariables = true;
    testSame(options, "function f(c) {var f = c; arguments[0] = this;" +
                      "    f.apply(this, arguments); return this;}");
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue550
  public void testIssue550() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.foldConstants = true;
    options.inlineVariables = true;
    options.flowSensitiveInlineVariables = true;
    test(options,
        "function f(h) {\n" +
        "  var a = h;\n" +
        "  a = a + 'x';\n" +
        "  a = a + 'y';\n" +
        "  return a;\n" +
        "}",
        "function f(a) {return a + 'xy'}");
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue284
  public void testIssue284() {
    CompilerOptions options = createCompilerOptions();
    options.smartNameRemoval = true;
    test(options,
        "var goog = {};" +
        "goog.inherits = function(x, y) {};" +
        "var ns = {};" +
        "" +
        "ns.PageSelectionModel = function() {};" +
        "" +
        "ns.PageSelectionModel.FooEvent = function() {};" +
        "" +
        "ns.PageSelectionModel.SelectEvent = function() {};" +
        "goog.inherits(ns.PageSelectionModel.ChangeEvent," +
        "    ns.PageSelectionModel.FooEvent);",
        "");
  }

// com.google.javascript.jscomp.IntegrationTest::testCodingConvention
  public void testCodingConvention() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    assertEquals(
      compiler.getCodingConvention().getClass().toString(),
      ClosureCodingConvention.class.toString());
  }

// com.google.javascript.jscomp.IntegrationTest::testJQueryStringSplitLoops
  public void testJQueryStringSplitLoops() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    test(options,
      "var x=['1','2','3','4','5','6','7']",
      "var x='1,2,3,4,5,6,7'.split(',')");

    options = createCompilerOptions();
    options.foldConstants = true;
    options.computeFunctionSideEffects = false;
    options.removeUnusedVars = true;

    
    test(options,
      "var x=['1','2','3','4','5','6','7']",
      "");

  }

// com.google.javascript.jscomp.IntegrationTest::testAlwaysRunSafetyCheck
  public void testAlwaysRunSafetyCheck() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = false;
    options.customPasses = ArrayListMultimap.create();
    options.customPasses.put(
        CustomPassExecutionTime.BEFORE_OPTIMIZATIONS,
        new CompilerPass() {
          @Override public void process(Node externs, Node root) {
            Node var = root.getLastChild().getFirstChild();
            assertEquals(Token.VAR, var.getType());
            var.detachFromParent();
          }
        });
    try {
      test(options,
           "var x = 3; function f() { return x + z; }",
           "function f() { return x + z; }");
      fail("Expected runtime exception");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().indexOf("Unexpected variable x") != -1);
    }
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressEs5StrictWarning
  public void testSuppressEs5StrictWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.ES5_STRICT, CheckLevel.WARNING);
    testSame(options,
        "\n" +
        "function f() { var arguments; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckProvidesWarning
  public void testCheckProvidesWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_PROVIDES, CheckLevel.WARNING);
    options.setCheckProvides(CheckLevel.WARNING);
    test(options,
        "\n" +
        "function f() { var arguments; }",
        DiagnosticType.warning("JSC_MISSING_PROVIDE", "missing goog.provide(''{0}'')"));
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressCheckProvidesWarning
  public void testSuppressCheckProvidesWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_PROVIDES, CheckLevel.WARNING);
    options.setCheckProvides(CheckLevel.WARNING);
    testSame(options,
        "\n" +
        "function f() { var arguments; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testRenamePrefixNamespace
  public void testRenamePrefixNamespace() {
    String code =
        "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseProperties = true;
    options.renamePrefixNamespace = "_";
    test(options, code, "_.x$FOO = 5; _.x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testRenamePrefixNamespaceActivatesMoveFunctionDeclarations
  public void testRenamePrefixNamespaceActivatesMoveFunctionDeclarations() {
    CompilerOptions options = createCompilerOptions();
    String code = "var x = f; function f() { return 3; }";
    testSame(options, code);
    assertFalse(options.moveFunctionDeclarations);
    options.renamePrefixNamespace = "_";
    test(options, code, "_.f = function() { return 3; }; _.x = _.f;");
  }

// com.google.javascript.jscomp.IntegrationTest::testBrokenNameSpace
  public void testBrokenNameSpace() {
    CompilerOptions options = createCompilerOptions();
    String code = "var goog; goog.provide('i.am.on.a.Horse');" +
                  "i.am.on.a.Horse = function() {};" +
                  "i.am.on.a.Horse.prototype.x = function() {};" +
                  "i.am.on.a.Boat.prototype.y = function() {}";
    options.closurePass = true;
    options.collapseProperties = true;
    options.smartNameRemoval = true;
    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testNamelessParameter
  public void testNamelessParameter() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    String code =
        "var impl_0;" +
        "$load($init());" +
        "function $load(){" +
        "  window['f'] = impl_0;" +
        "}" +
        "function $init() {" +
        "  impl_0 = {};" +
        "}";
    String result =
        "window.f = {};";
    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testHiddenSideEffect
  public void testHiddenSideEffect() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.setAliasExternals(true);
    String code =
        "window.offsetWidth;";
    String result =
        "window.offsetWidth;";
    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testNegativeZero
  public void testNegativeZero() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    test(options,
        "function bar(x) { return x; }\n" +
        "function foo(x) { print(x / bar(0));\n" +
        "                 print(x / bar(-0)); }\n" +
        "foo(3);",
        "print(3/0);print(3/-0);");
  }

// com.google.javascript.jscomp.IntegrationTest::testSingletonGetter1
  public void testSingletonGetter1() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.setCodingConvention(new ClosureCodingConvention());
    test(options,
        "\n" +
        "var goog = goog || {};\n" +
        "goog.addSingletonGetter = function(ctor) {\n" +
        "  ctor.getInstance = function() {\n" +
        "    return ctor.instance_ || (ctor.instance_ = new ctor());\n" +
        "  };\n" +
        "};" +
        "function Foo() {}\n" +
        "goog.addSingletonGetter(Foo);" +
        "Foo.prototype.bar = 1;" +
        "function Bar() {}\n" +
        "goog.addSingletonGetter(Bar);" +
        "Bar.prototype.bar = 1;",
        "");
  }

// com.google.javascript.jscomp.IntegrationTest::testIncompleteFunction
  public void testIncompleteFunction() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "var foo = {bar: function(e) }" },
        new String[] { "var foo = {bar: function(e){}};" },
        warnings
    );
  }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }
