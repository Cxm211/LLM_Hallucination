// buggy code
  String getReadableJSTypeName(Node n, boolean dereference) {

    // The best type name is the actual type name.

    // If we're analyzing a GETPROP, the property may be inherited by the
    // prototype chain. So climb the prototype chain and find out where
    // the property was originally defined.
    if (n.isGetProp()) {
      ObjectType objectType = getJSType(n.getFirstChild()).dereference();
      if (objectType != null) {
        String propName = n.getLastChild().getString();
        if (objectType.getConstructor() != null &&
            objectType.getConstructor().isInterface()) {
          objectType = FunctionType.getTopDefiningInterface(
              objectType, propName);
        } else {
          // classes
          while (objectType != null && !objectType.hasOwnProperty(propName)) {
            objectType = objectType.getImplicitPrototype();
          }
        }

        // Don't show complex function names or anonymous types.
        // Instead, try to get a human-readable type name.
        if (objectType != null &&
            (objectType.getConstructor() != null ||
             objectType.isFunctionPrototypeType())) {
          return objectType.toString() + "." + propName;
        }
      }
    }

    JSType type = getJSType(n);
    if (dereference) {
      ObjectType dereferenced = type.dereference();
      if (dereferenced != null) {
        type = dereferenced;
      }
    }
    if (type.isFunctionPrototypeType() ||
        (type.toObjectType() != null &&
         type.toObjectType().getConstructor() != null)) {
      return type.toString();
    }
    String qualifiedName = n.getQualifiedName();
    if (qualifiedName != null) {
      return qualifiedName;
    } else if (type.isFunctionType()) {
      // Don't show complex function names.
      return "function";
    } else {
      return type.toString();
    }
  }

// relevant test
// com.google.javascript.jscomp.IntegrationTest::testTypeNameParser
  public void testTypeNameParser() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    test(options, " var n = window.name;",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testMemoizedTypedScopeCreator
  public void testMemoizedTypedScopeCreator() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.ambiguateProperties = true;
    options.propertyRenaming = PropertyRenamingPolicy.ALL_UNQUOTED;
    test(options, "function someTest() {\n"
        + "  \n"
        + "  function Foo() { this.instProp = 3; }\n"
        + "  Foo.prototype.protoProp = function(a, b) {};\n"
        + "  \n"
        + "  function Bar() {}\n"
        + "  goog.inherits(Bar, Foo);\n"
        + "  var o = new Bar();\n"
        + "  o.protoProp(o.protoProp, o.instProp);\n"
        + "}",
        "function someTest() {\n"
        + "  function Foo() { this.b = 3; }\n"
        + "  function Bar() {}\n"
        + "  Foo.prototype.a = function(a, b) {};\n"
        + "  goog.c(Bar, Foo);\n"
        + "  var o = new Bar();\n"
        + "  o.a(o.a, o.b);\n"
        + "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckTypes
  public void testCheckTypes() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    test(options, "var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.IntegrationTest::testReplaceCssNames
  public void testReplaceCssNames() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.gatherCssNames = true;
    test(options, "\n"
         + "var COMPILED = false;\n"
         + "goog.setCssNameMapping({'foo':'bar'});\n"
         + "function getCss() {\n"
         + "  return goog.getCssName('foo');\n"
         + "}",
         "var COMPILED = true;\n"
         + "function getCss() {\n"
         + "  return \"bar\";"
         + "}");
    assertEquals(
        ImmutableMap.of("foo", new Integer(1)),
        lastCompiler.getPassConfig().getIntermediateState().cssNames);
  }

// com.google.javascript.jscomp.IntegrationTest::testReplaceIdGeneratorsTest
  public void testReplaceIdGeneratorsTest() {
    CompilerOptions options = createCompilerOptions();
    options.replaceIdGenerators = true;

    options.setIdGenerators(ImmutableMap.<String, RenamingMap>of(
        "xid", new RenamingMap() {
      @Override
      public String get(String value) {
        return ":" + value + ":";
      }
    }));

    test(options, ""
         + "var xid = function() {};\n"
         + "function f() {\n"
         + "  return xid('foo');\n"
         + "}",
         "var xid = function() {};\n"
         + "function f() {\n"
         + "  return ':foo:';\n"
         + "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveClosureAsserts
  public void testRemoveClosureAsserts() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    testSame(options,
        "var goog = {};"
        + "goog.asserts.assert(goog);");
    options.removeClosureAsserts = true;
    test(options,
        "var goog = {};"
        + "goog.asserts.assert(goog);",
        "var goog = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testDeprecation
  public void testDeprecation() {
    String code = " function f() { } function g() { f(); }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.setWarningLevel(DiagnosticGroups.DEPRECATED, CheckLevel.ERROR);
    testSame(options, code);

    options.checkTypes = true;
    test(options, code, CheckAccessControls.DEPRECATED_NAME);
  }

// com.google.javascript.jscomp.IntegrationTest::testVisibility
  public void testVisibility() {
    String[] code = {
        " function f() { }",
        "function g() { f(); }"
    };

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.setWarningLevel(DiagnosticGroups.VISIBILITY, CheckLevel.ERROR);
    testSame(options, code);

    options.checkTypes = true;
    test(options, code, CheckAccessControls.BAD_PRIVATE_GLOBAL_ACCESS);
  }

// com.google.javascript.jscomp.IntegrationTest::testUnreachableCode
  public void testUnreachableCode() {
    String code = "function f() { return \n 3; }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.checkUnreachableCode = CheckLevel.ERROR;
    test(options, code, CheckUnreachableCode.UNREACHABLE_CODE);
  }

// com.google.javascript.jscomp.IntegrationTest::testMissingReturn
  public void testMissingReturn() {
    String code =
        " function f() { if (f) { return 3; } }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.checkMissingReturn = CheckLevel.ERROR;
    testSame(options, code);

    options.checkTypes = true;
    test(options, code, CheckMissingReturn.MISSING_RETURN_STATEMENT);
  }

// com.google.javascript.jscomp.IntegrationTest::testIdGenerators
  public void testIdGenerators() {
    String code =  "function f() {} f('id');";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.setIdGenerators(Sets.newHashSet("f"));
    test(options, code, "function f() {} 'a';");
  }

// com.google.javascript.jscomp.IntegrationTest::testOptimizeArgumentsArray
  public void testOptimizeArgumentsArray() {
    String code =  "function f() { return arguments[0]; }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.optimizeArgumentsArray = true;
    String argName = "JSCompiler_OptimizeArgumentsArray_p0";
    test(options, code,
         "function f(" + argName + ") { return " + argName + "; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testOptimizeParameters
  public void testOptimizeParameters() {
    String code = "function f(a) { return a; } f(true);";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.optimizeParameters = true;
    test(options, code, "function f() { var a = true; return a;} f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testOptimizeReturns
  public void testOptimizeReturns() {
    String code = "function f(a) { return a; } f(true);";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.optimizeReturns = true;
    test(options, code, "function f(a) {return;} f(true);");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveAbstractMethods
  public void testRemoveAbstractMethods() {
    String code = CLOSURE_BOILERPLATE +
        "var x = {}; x.foo = goog.abstractMethod; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.closurePass = true;
    options.collapseProperties = true;
    test(options, code, CLOSURE_COMPILED + " var x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testGoogDefine1
  public void testGoogDefine1() {
    String code = CLOSURE_BOILERPLATE +
        " goog.define('FLAG', true);";

    CompilerOptions options = createCompilerOptions();

    options.closurePass = true;
    options.collapseProperties = true;
    options.setDefineToBooleanLiteral("FLAG", false);

    test(options, code, CLOSURE_COMPILED + " var FLAG = false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testGoogDefine2
  public void testGoogDefine2() {
    String code = CLOSURE_BOILERPLATE +
        "goog.provide('ns');" +
        " goog.define('ns.FLAG', true);";

    CompilerOptions options = createCompilerOptions();

    options.closurePass = true;
    options.collapseProperties = true;
    options.setDefineToBooleanLiteral("ns.FLAG", false);
    test(options, code, CLOSURE_COMPILED + "var ns$FLAG = false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseProperties1
  public void testCollapseProperties1() {
    String code =
        "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseProperties = true;
    test(options, code, "var x$FOO = 5; var x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseProperties2
  public void testCollapseProperties2() {
    String code =
        "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseProperties = true;
    options.collapseObjectLiterals = true;
    test(options, code, "var x$FOO = 5; var x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseObjectLiteral1
  public void testCollapseObjectLiteral1() {
    
    String code = "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseObjectLiterals = true;
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseObjectLiteral2
  public void testCollapseObjectLiteral2() {
    String code =
        "function f() {var x = {}; x.FOO = 5; x.bar = 3;}";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseObjectLiterals = true;
    test(options, code,
        "function f(){" +
        "var JSCompiler_object_inline_FOO_0;" +
        "var JSCompiler_object_inline_bar_1;" +
        "JSCompiler_object_inline_FOO_0=5;" +
        "JSCompiler_object_inline_bar_1=3}");
  }

// com.google.javascript.jscomp.IntegrationTest::testTightenTypesWithoutTypeCheck
  public void testTightenTypesWithoutTypeCheck() {
    CompilerOptions options = createCompilerOptions();
    options.tightenTypes = true;
    test(options, "", DefaultPassConfig.TIGHTEN_TYPES_WITHOUT_TYPE_CHECK);
  }

// com.google.javascript.jscomp.IntegrationTest::testDisambiguateProperties
  public void testDisambiguateProperties() {
    String code =
        " function Foo(){} Foo.prototype.bar = 3;" +
        " function Baz(){} Baz.prototype.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.disambiguateProperties = true;
    options.checkTypes = true;
    test(options, code,
         "function Foo(){} Foo.prototype.Foo_prototype$bar = 3;" +
         "function Baz(){} Baz.prototype.Baz_prototype$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testMarkPureCalls
  public void testMarkPureCalls() {
    String testCode = "function foo() {} foo();";
    CompilerOptions options = createCompilerOptions();
    options.removeDeadCode = true;

    testSame(options, testCode);

    options.computeFunctionSideEffects = true;
    test(options, testCode, "function foo() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testMarkNoSideEffects
  public void testMarkNoSideEffects() {
    String testCode = "noSideEffects();";
    CompilerOptions options = createCompilerOptions();
    options.removeDeadCode = true;

    testSame(options, testCode);

    options.markNoSideEffectCalls = true;
    test(options, testCode, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testChainedCalls
  public void testChainedCalls() {
    CompilerOptions options = createCompilerOptions();
    options.chainCalls = true;
    test(
        options,
        " function Foo() {} " +
        "Foo.prototype.bar = function() { return this; }; " +
        "var f = new Foo();" +
        "f.bar(); " +
        "f.bar(); ",
        "function Foo() {} " +
        "Foo.prototype.bar = function() { return this; }; " +
        "var f = new Foo();" +
        "f.bar().bar();");
  }

// com.google.javascript.jscomp.IntegrationTest::testExtraAnnotationNames
  public void testExtraAnnotationNames() {
    CompilerOptions options = createCompilerOptions();
    options.setExtraAnnotationNames(Sets.newHashSet("TagA", "TagB"));
    test(
        options,
        " var f = new Foo();  f.bar();",
        "var f = new Foo(); f.bar();");
  }

// com.google.javascript.jscomp.IntegrationTest::testDevirtualizePrototypeMethods
  public void testDevirtualizePrototypeMethods() {
    CompilerOptions options = createCompilerOptions();
    options.devirtualizePrototypeMethods = true;
    test(
        options,
        " var Foo = function() {}; " +
        "Foo.prototype.bar = function() {};" +
        "(new Foo()).bar();",
        "var Foo = function() {};" +
        "var JSCompiler_StaticMethods_bar = " +
        "    function(JSCompiler_StaticMethods_bar$self) {};" +
        "JSCompiler_StaticMethods_bar(new Foo());");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckConsts
  public void testCheckConsts() {
    CompilerOptions options = createCompilerOptions();
    options.inlineConstantVars = true;
    test(options, "var FOO = true; FOO = false",
        ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testAllChecksOn
  public void testAllChecksOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkSuspiciousCode = true;
    options.checkControlStructures = true;
    options.checkRequires = CheckLevel.ERROR;
    options.checkProvides = CheckLevel.ERROR;
    options.generateExports = true;
    options.exportTestFunctions = true;
    options.closurePass = true;
    options.checkMissingGetCssNameLevel = CheckLevel.ERROR;
    options.checkMissingGetCssNameBlacklist = "goog";
    options.syntheticBlockStartMarker = "synStart";
    options.syntheticBlockEndMarker = "synEnd";
    options.checkSymbols = true;
    options.aggressiveVarCheck = CheckLevel.ERROR;
    options.processObjectPropertyString = true;
    options.collapseProperties = true;
    test(options, CLOSURE_BOILERPLATE, CLOSURE_COMPILED);
  }

// com.google.javascript.jscomp.IntegrationTest::testTypeCheckingWithSyntheticBlocks
  public void testTypeCheckingWithSyntheticBlocks() {
    CompilerOptions options = createCompilerOptions();
    options.syntheticBlockStartMarker = "synStart";
    options.syntheticBlockEndMarker = "synEnd";
    options.checkTypes = true;

    
    
    
    testSame(
        options,
        " function f(x) {}" +
        "function g() {" +
        " synStart('foo');" +
        " var progress = 1;" +
        " f(progress);" +
        " synEnd('foo');" +
        "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCompilerDoesNotBlowUpIfUndefinedSymbols
  public void testCompilerDoesNotBlowUpIfUndefinedSymbols() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = true;

    
    options.setWarningLevel(
        DiagnosticGroup.forType(VarCheck.UNDEFINED_VAR_ERROR),
        CheckLevel.OFF);

    
    testSame(options, "var x = {foo: y};");
  }

// com.google.javascript.jscomp.IntegrationTest::testConstantTagsMustAlwaysBeRemoved
  public void testConstantTagsMustAlwaysBeRemoved() {
    CompilerOptions options = createCompilerOptions();

    options.variableRenaming = VariableRenamingPolicy.LOCAL;
    String originalText = "var G_GEO_UNKNOWN_ADDRESS=1;\n" +
        "function foo() {" +
        "  var localVar = 2;\n" +
        "  if (G_GEO_UNKNOWN_ADDRESS == localVar) {\n" +
        "    alert(\"A\"); }}";
    String expectedText = "var G_GEO_UNKNOWN_ADDRESS=1;" +
        "function foo(){var a=2;if(G_GEO_UNKNOWN_ADDRESS==a){alert(\"A\")}}";

    test(options, originalText, expectedText);
  }

// com.google.javascript.jscomp.IntegrationTest::testClosurePassPreservesJsDoc
  public void testClosurePassPreservesJsDoc() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.closurePass = true;

    test(options,
         CLOSURE_BOILERPLATE +
         "goog.provide('Foo');  Foo = function() {};" +
         "var x = new Foo();",
         "var COMPILED=true;var goog={};goog.exportSymbol=function(){};" +
         "var Foo=function(){};var x=new Foo");
    test(options,
         CLOSURE_BOILERPLATE +
         "goog.provide('Foo');  Foo = {a: 3};",
         TypeCheck.ENUM_NOT_CONSTANT);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst
  public void testProvidedNamespaceIsConst() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo'); " +
         "function f() { foo = {};}",
         "var foo = {}; function f() { foo = {}; }",
         ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

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
         "var foo = {}; foo = {}; foo.Bar = {};");
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

// com.google.javascript.jscomp.IntegrationTest::testNameAnonymousFunctionsWithVarRemoval
  public void testNameAnonymousFunctionsWithVarRemoval() {
    CompilerOptions options = createCompilerOptions();
    options.setRemoveUnusedVariables(CompilerOptions.Reach.LOCAL_ONLY);
    options.setInlineVariables(true);
    String code = "var f = function longName() {}; var g = function() {};" +
        "function longerName() {} var i = longerName;";
    test(options, code,
         "var f = function() {}; var g = function() {}; " +
         "var i = function() {};");

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.MAPPED;
    test(options, code,
         "var f = function longName() {}; var g = function $() {};" +
         "var i = function longerName(){};");
    assertNotNull(lastCompiler.getResult().namedAnonFunctionMap);

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.UNMAPPED;
    test(options, code,
         "var f = function longName() {}; var g = function $g$() {};" +
         "var i = function longerName(){};");
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

      
      if (letter >= 'i') {
        letter++;
      }
      if (letter >= 'j') {
        letter++;
      }
      if (letter >= 'o') {
        letter++;
      }

      expected += "a.argz = function() {arguments};";
      expected += "function " + letter + "(c){}";
    }

    code += "var F = new f(); F.argz();";
    expected += "var q = new b(); q.argz();";

    for (int i = 0; i < 10; i++) {
      code += "F.devir" + i + "();";

      char letter = (char) ('d' + i);

      
      if (letter >= 'i') {
        letter++;
      }
      if (letter >= 'j') {
        letter++;
      }
      if (letter >= 'o') {
        letter++;
      }

      expected += letter + "(q);";
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
    String code = "longLabel: for(;true;) { break longLabel; }";
    String expected = "a: for(;true;) { break a; }";
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

    options.collapseVariableDeclarations = true;

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
    externs = ImmutableList.of(
        SourceFile.fromCode("externs", "extern.foo"));

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
    externs = ImmutableList.of(SourceFile.fromCode("externs",
        "var externs = {};  var externs = {};"));
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
        "getters are not supported in older versions of JavaScript. " +
        "If you are targeting newer versions of JavaScript, " +
        "set the appropriate language_in option. " +
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

    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue701
  public void testIssue701() {
    
    String ascii = "";
    String result = "\n";
    testSame(createCompilerOptions(), ascii);
    assertEquals(result, lastCompiler.toSource());
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue724
  public void testIssue724() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    String code =
        "isFunction = function(functionToCheck) {" +
        "  var getType = {};" +
        "  return functionToCheck && " +
        "      getType.toString.apply(functionToCheck) === " +
        "     '[object Function]';" +
        "};";
    String result =
        "isFunction=function(a){var b={};" +
        "return a&&\"[object Function]\"===b.b.a(a)}";

    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue730
  public void testIssue730() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);

    String code =
        "function A() {this.foo = 0; Object.seal(this);}\n" +
        "function B() {this.a = new A();}\n" +
        "B.prototype.dostuff = function() {this.a.foo++;alert('hi');}\n" +
        "new B().dostuff();\n";

    test(options,
        code,
        "function a(){this.b=0;Object.seal(this)}" +
        "(new function(){this.a=new a}).a.b++;" +
        "alert(\"hi\")");

    options.removeUnusedClassProperties = true;

    
    test(options,
        code,
        "function a(){this.b=0;Object.seal(this)}" +
        "(new function(){this.a=new a}).a.b++;" +
        "alert(\"hi\")");
  }

// com.google.javascript.jscomp.IntegrationTest::testAddFunctionProperties1
  public void testAddFunctionProperties1() throws Exception {
    String source =
        "var Foo = {};" +
        "var addFuncProp = function(o) {" +
        "  o.f = function() {}" +
        "};" +
        "addFuncProp(Foo);" +
        "alert(Foo.f());";
    String expected =
        "alert(void 0);";
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.setRenamingPolicy(
        VariableRenamingPolicy.OFF, PropertyRenamingPolicy.OFF);
    test(options, source, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testAddFunctionProperties2
  public void testAddFunctionProperties2() throws Exception {
    String source =
        " function F() {}" +
        "var x = new F();" +
        "" +
        "function g() { this.bar = function() { alert(3); }; }" +
        "g.call(x);" +
        "x.bar();";
    String expected =
        "var x = new function() {};" +
        "" +
        "(function () { this.bar = function() { alert(3); }; }).call(x);" +
        "x.bar();";

    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.setRenamingPolicy(
        VariableRenamingPolicy.OFF, PropertyRenamingPolicy.OFF);
    test(options, source, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testAddFunctionProperties3
  public void testAddFunctionProperties3() throws Exception {
    String source =
        " function F() {}" +
        "var x = new F();" +
        "" +
        "function g(y) { y.bar = function() { alert(3); }; }" +
        "g(x);" +
        "x.bar();";
    String expected =
        "var x = new function() {};" +
        "" +
        "(function (y) { y.bar = function() { alert(3); }; })(x);" +
        "x.bar();";

    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.setRenamingPolicy(
        VariableRenamingPolicy.OFF, PropertyRenamingPolicy.OFF);
    test(options, source, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testAddFunctionProperties4
  public void testAddFunctionProperties4() throws Exception {
    String source =
        "" +
        "var Foo = function() {};" +
        "var goog = {};" +
        "goog.addSingletonGetter = function(o) {" +
        "  o.f = function() {" +
        "    o.i = new o;" +
        "  };" +
        "};" +
        "goog.addSingletonGetter(Foo);" +
        "alert(Foo.f());";
    String expected =
        "function Foo(){} Foo.f=function(){Foo.i=new Foo}; alert(Foo.f());";

    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.setRenamingPolicy(
        VariableRenamingPolicy.OFF, PropertyRenamingPolicy.OFF);
    test(options, source, expected);
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
        "if (((x < 1 || x > 1) || 1 < x) || 1 > x) { alert(x) }",
        "   (((1 > x || 1 < x) || 1 < x) || 1 > x) && alert(x) ");
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

// com.google.javascript.jscomp.IntegrationTest::testNoFuseIntoSyntheticBlock
  public void testNoFuseIntoSyntheticBlock() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    options.syntheticBlockStartMarker = "START";
    options.syntheticBlockEndMarker = "END";
    options.aggressiveFusion = false;
    testSame(options, "for(;;) { x = 1; {START(); {z = 3} END()} }");
    testSame(options, "x = 1; y = 2; {START(); {z = 3} END()} f()");
    options.aggressiveFusion = true;
    testSame(options, "x = 1; {START(); {z = 3} END()} f()");
    test(options, "x = 1; y = 3; {START(); {z = 3} END()} f()",
                  "x = 1, y = 3; {START(); {z = 3} END()} f()");
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
        
        "function f(a) { a += 'x'; return a += 'y'; }");
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

// com.google.javascript.jscomp.IntegrationTest::testIssue772
  public void testIssue772() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.checkTypes = true;
    test(
        options,
        " var a = {};" +
        " a.b = {};" +
        " a.b.c = {};" +
        "goog.scope(function() {" +
        "  var b = a.b;" +
        "  var c = b.c;" +
        "  " +
        "  c.MyType;" +
        "  " +
        "  c.myFunc = function(x) {};" +
        "});",
        " var a = {};" +
        " a.b = {};" +
        " a.b.c = {};" +
        "a.b.c.MyType;" +
        "a.b.c.myFunc = function(x) {};");
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
      "var x='1234567'.split('')");

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
      fail("Expected run-time exception");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().indexOf("Unexpected variable x") != -1);
    }
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressEs5StrictWarning
  public void testSuppressEs5StrictWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.ES5_STRICT, CheckLevel.WARNING);
    test(options,
        "\n" +
        "function f() { var arguments; }",
        "function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckProvidesWarning
  public void testCheckProvidesWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_PROVIDES,
        CheckLevel.WARNING);
    options.setCheckProvides(CheckLevel.WARNING);
    test(options,
        "\n" +
        "function f() { var arguments; }",
        DiagnosticType
        .warning("JSC_MISSING_PROVIDE", "missing goog.provide(''{0}'')"));
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressCheckProvidesWarning
  public void testSuppressCheckProvidesWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_PROVIDES,
        CheckLevel.WARNING);
    options.setCheckProvides(CheckLevel.WARNING);
    testSame(options,
        "\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressCastWarning
  public void testSuppressCastWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);

    normalizeResults = true;

    test(options,
        "function f() { var xyz =  (0); }",
        DiagnosticType.warning(
            "JSC_INVALID_CAST", "invalid cast"));

    testSame(options,
        "\n" +
        "function f() { var xyz =  (0); }");

    testSame(options,
        " var g = {};" +
        "" +
        "g.a = g.b = function() { var xyz =  (0); }");
  }

// com.google.javascript.jscomp.IntegrationTest::testLhsCast
  public void testLhsCast() {
    CompilerOptions options = createCompilerOptions();
    test(
        options,
        " var g = {};" +
        " (g.foo) = 3;",
        " var g = {};" +
        "g.foo = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testRenamePrefix
  public void testRenamePrefix() {
    String code = "var x = {}; function f(y) {}";
    CompilerOptions options = createCompilerOptions();
    options.renamePrefix = "G_";
    options.variableRenaming = VariableRenamingPolicy.ALL;
    test(options, code, "var G_={}; function G_a(a) {}");
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

// com.google.javascript.jscomp.IntegrationTest::testRenamePrefixNamespaceProtectSideEffects
  public void testRenamePrefixNamespaceProtectSideEffects() {
    String code = "var x = null; try { +x.FOO; } catch (e) {}";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(
        options);
    options.renamePrefixNamespace = "_";
    test(options, code, "_.x = null; try { +_.x.FOO; } catch (e) {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testRenameCollision
  public void testRenameCollision() {
    String code = "" +
          "" +
          "var x = {};\ntry {\n(0,use)(x.FOO);\n} catch (e) {}";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(
        options);
    options.renamePrefixNamespace = "a";
    options.setVariableRenaming(VariableRenamingPolicy.ALL);
    options.setRenamePrefixNamespaceAssumeCrossModuleNames(false);
    WarningLevel.DEFAULT.setOptionsForWarningLevel(options);

    test(options, code,
        "var b = {}; try { (0,window.use)(b.FOO); } catch (c) {}");
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

// com.google.javascript.jscomp.IntegrationTest::testIncompleteFunction1
  public void testIncompleteFunction1() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "var foo = {bar: function(e) }" },
        new String[] { "var foo = {bar: function(e){}};" },
        warnings
    );
  }

// com.google.javascript.jscomp.IntegrationTest::testIncompleteFunction2
  public void testIncompleteFunction2() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "function hi" },
        new String[] { "function hi() {}" },
        warnings
    );
  }

// com.google.javascript.jscomp.IntegrationTest::testSortingOff
  public void testSortingOff() {
    CompilerOptions options = new CompilerOptions();
    options.closurePass = true;
    options.setCodingConvention(new ClosureCodingConvention());
    test(options,
         new String[] {
           "goog.require('goog.beer');",
           "goog.provide('goog.beer');"
         },
         ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testUnboundedArrayLiteralInfiniteLoop
  public void testUnboundedArrayLiteralInfiniteLoop() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    test(options,
         "var x = [1, 2",
         "var x = [1, 2]",
         RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvideRequireSameFile
  public void testProvideRequireSameFile() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.setDependencyOptions(
        new DependencyOptions()
        .setDependencySorting(true));
    options.closurePass = true;
    test(
        options,
        "goog.provide('x');\ngoog.require('x');",
        "var x = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testDependencySorting
  public void testDependencySorting() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.setDependencyOptions(
        new DependencyOptions()
        .setDependencySorting(true));
    test(
        options,
        new String[] {
          "goog.require('x');",
          "goog.provide('x');",
        },
        new String[] {
          "goog.provide('x');",
          "goog.require('x');",

          
          
          "",
        });
  }

// com.google.javascript.jscomp.IntegrationTest::testStrictWarningsGuard
  public void testStrictWarningsGuard() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.addWarningsGuard(new StrictWarningsGuard());

    Compiler compiler = compile(options,
        " function f() { return true; }");
    assertEquals(1, compiler.getErrors().length);
    assertEquals(0, compiler.getWarnings().length);
  }

// com.google.javascript.jscomp.IntegrationTest::testStrictWarningsGuardEmergencyMode
  public void testStrictWarningsGuardEmergencyMode() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.addWarningsGuard(new StrictWarningsGuard());
    options.useEmergencyFailSafe();

    Compiler compiler = compile(options,
        " function f() { return true; }");
    assertEquals(0, compiler.getErrors().length);
    assertEquals(1, compiler.getWarnings().length);
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineProperties
  public void testInlineProperties() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    level.setTypeBasedOptimizationOptions(options);

    String code = "" +
        "var ns = {};\n" +
        "\n" +
        "ns.C = function () {this.someProperty = 1}\n" +
        "alert(new ns.C().someProperty + new ns.C().someProperty);\n";
    assertTrue(options.inlineProperties);
    assertTrue(options.collapseProperties);
    
    test(options, code, "alert(2);");
  }

// com.google.javascript.jscomp.IntegrationTest::testGoogDefineClass1
  public void testGoogDefineClass1() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    level.setTypeBasedOptimizationOptions(options);

    String code = "" +
        "var ns = {};\n" +
        "ns.C = goog.defineClass(null, {\n" +
        "  \n" +
        "  constructor: function () {this.someProperty = 1}\n" +
        "});\n" +
        "alert(new ns.C().someProperty + new ns.C().someProperty);\n";
    assertTrue(options.inlineProperties);
    assertTrue(options.collapseProperties);
    
    test(options, code, "alert(2);");
  }

// com.google.javascript.jscomp.IntegrationTest::testGoogDefineClass2
  public void testGoogDefineClass2() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    level.setTypeBasedOptimizationOptions(options);

    String code = "" +
        "var C = goog.defineClass(null, {\n" +
        "  \n" +
        "  constructor: function () {this.someProperty = 1}\n" +
        "});\n" +
        "alert(new C().someProperty + new C().someProperty);\n";
    assertTrue(options.inlineProperties);
    assertTrue(options.collapseProperties);
    
    test(options, code, "alert(2);");
  }

// com.google.javascript.jscomp.IntegrationTest::testGoogDefineClass3
  public void testGoogDefineClass3() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    level.setTypeBasedOptimizationOptions(options);
    WarningLevel warnings = WarningLevel.VERBOSE;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "var C = goog.defineClass(null, {\n" +
        "  \n" +
        "  constructor: function () {\n" +
        "    \n" +
        "    this.someProperty = 1},\n" +
        "  \n" +
        "  someMethod: function (a) {}\n" +
        "});" +
        "var x = new C();\n" +
        "x.someMethod(x.someProperty);\n";
    assertTrue(options.inlineProperties);
    assertTrue(options.collapseProperties);
    
    test(options, code, TypeValidator.TYPE_MISMATCH_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckConstants1
  public void testCheckConstants1() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.QUIET;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "var foo; foo();\n" +
        "\n" +
        "var x = 1; foo(); x = 2;\n";
    test(options, code, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckConstants2
  public void testCheckConstants2() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "var foo;\n" +
        "\n" +
        "var x = 1; foo(); x = 2;\n";
    test(options, code, ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testBiasedLabelRenaming
  public void testBiasedLabelRenaming() {
    CompilerOptions options = createCompilerOptions();
    options.setAggressiveRenaming(true);
    options.setLabelRenaming(true);
    String code = "function a() {lbl: while(1) {while(1) {break lbl}}}";
    String result = "function a() {f: for(;1;) for(;1;)break f}";
    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue937
  public void testIssue937() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "console.log(" +
            " ((new x())['abc'])());";
    String result = "" +
        "console.log((new x()).abc());";
    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue787
  public void testIssue787() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "function some_function() {\n" +
        "  var fn1;\n" +
        "  var fn2;\n" +
        "\n" +
        "  if (any_expression) {\n" +
        "    fn2 = external_ref;\n" +
        "    fn1 = function (content) {\n" +
        "      return fn2();\n" +
        "    }\n" +
        "  }\n" +
        "\n" +
        "  return {\n" +
        "    method1: function () {\n" +
        "      if (fn1) fn1();\n" +
        "      return true;\n" +
        "    },\n" +
        "    method2: function () {\n" +
        "      return false;\n" +
        "    }\n" +
        "  }\n" +
        "}";

    String result = "" +
        "function some_function() {\n" +
        "  var a, b;\n" +
        "  any_expression && (b = external_ref, a = function(a) {\n" +
        "    return b()\n" +
        "  });\n" +
        "  return{method1:function() {\n" +
        "    a && a();\n" +
        "    return !0\n" +
        "  }, method2:function() {\n" +
        "    return !1\n" +
        "  }}\n" +
        "}\n" +
        "";

    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testExports
  public void testExports() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        " var X = function() {" +
           " this.abc = 1;};\n" +
        " var Y = function() {" +
           " this.abc = 1;};\n" +
        "alert(new X().abc + new Y().abc);";

    
    test(options, code,
        "alert((new function(){this.a = 1}).a + " +
            "(new function(){this.a = 1}).a);");

    options.generateExports = true;

    
    test(options,
        " var X = function() {" +
        " this.abc = 1;};\n",
        FindExportableNodes.NON_GLOBAL_ERROR);

    options.exportLocalPropertyDefinitions = true;

    
    
    test(options, code,
        DefaultPassConfig.CANNOT_USE_EXPORT_LOCALS_AND_EXTERN_PROP_REMOVAL);

    options.removeUnusedPrototypePropertiesInExterns = false;

    
    test(options, code,
        "alert((new function(){this.abc = 1}).abc + " +
            "(new function(){this.abc = 1}).abc);");

    
    test(options, "" +
        " var X = function() {" +
        " this.abc = 1;};\n" +
        " var Y = function() {" +
        " this.abc = 1;};\n" +
        "alert(new X() + new Y());",
        "alert((new function(){this.abc = 1}) + " +
            "(new function(){this.abc = 1}));");

    
    options.checkTypes = true;
    options.disambiguateProperties = true;
    options.ambiguateProperties = true;
    options.propertyInvalidationErrors = ImmutableMap.of(
        "abc", CheckLevel.ERROR);

    test(options, code,
        "alert((new function(){this.abc = 1}).abc + " +
            "(new function(){this.abc = 1}).abc);");

    
    test(options, "" +
        " var X = function() {" +
        " this.abc = 1;};\n" +
        " var Y = function() {" +
        " this.abc = 1;};\n" +
        "alert(new X() + new Y());",
        "alert((new function(){this.abc = 1}) + " +
            "(new function(){this.abc = 1}));");
  }

// com.google.javascript.jscomp.IntegrationTest::testManyAdds
  public void testManyAdds() {}

// com.google.javascript.jscomp.IntegrationTest::testIsEquivalentTo
  public void testIsEquivalentTo() {
    String[] input1 = {"function f(z) { return z; }"};
    String[] input2 = {"function f(y) { return y; }"};
    CompilerOptions options = new CompilerOptions();
    Node out1 = parse(input1, options, false);
    Node out2 = parse(input2, options, false);
    assertFalse(out1.isEquivalentTo(out2));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptOneLine
  public void testExcerptOneLine() throws Exception {
    assertEquals("foo:first line", provider.getSourceLine("foo", 1));
    assertEquals("foo:second line", provider.getSourceLine("foo", 2));
    assertEquals("foo:third line", provider.getSourceLine("foo", 3));
    assertEquals("bar:first line", provider.getSourceLine("bar", 1));
    assertEquals("bar:second line", provider.getSourceLine("bar", 2));
    assertEquals("bar:third line", provider.getSourceLine("bar", 3));
    assertEquals("bar:fourth line", provider.getSourceLine("bar", 4));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptLineFromInexistantSource
  public void testExcerptLineFromInexistantSource() throws Exception {
    assertEquals(null, provider.getSourceLine("inexistant", 1));
    assertEquals(null, provider.getSourceLine("inexistant", 7));
    assertEquals(null, provider.getSourceLine("inexistant", 90));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptInexistantLine
  public void testExcerptInexistantLine() throws Exception {
    assertEquals(null, provider.getSourceLine("foo", 0));
    assertEquals(null, provider.getSourceLine("foo", 4));
    assertEquals(null, provider.getSourceLine("bar", 0));
    assertEquals(null, provider.getSourceLine("bar", 5));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExceptNoNewLine
  public void testExceptNoNewLine() throws Exception {
    assertEquals("foo2:first line", provider.getSourceLine("foo2", 1));
    assertEquals("foo2:second line", provider.getSourceLine("foo2", 2));
    assertEquals("foo2:third line", provider.getSourceLine("foo2", 3));
    assertEquals(null, provider.getSourceLine("foo2", 4));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptRegion
  public void testExcerptRegion() throws Exception {
    assertRegionWellFormed("foo", 1);
    assertRegionWellFormed("foo", 2);
    assertRegionWellFormed("foo", 3);
    assertRegionWellFormed("bar", 1);
    assertRegionWellFormed("bar", 2);
    assertRegionWellFormed("bar", 3);
    assertRegionWellFormed("bar", 4);
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptRegionFromInexistantSource
  public void testExcerptRegionFromInexistantSource() throws Exception {
    assertEquals(null, provider.getSourceRegion("inexistant", 0));
    assertEquals(null, provider.getSourceRegion("inexistant", 6));
    assertEquals(null, provider.getSourceRegion("inexistant", 90));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptInexistantRegion
  public void testExcerptInexistantRegion() throws Exception {
    assertEquals(null, provider.getSourceRegion("foo", 0));
    assertEquals(null, provider.getSourceRegion("foo", 4));
    assertEquals(null, provider.getSourceRegion("bar", 0));
    assertEquals(null, provider.getSourceRegion("bar", 5));
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testModuleDepth
  public void testModuleDepth() {
    assertEquals("A should have depth 0", 0, A.getDepth());
    assertEquals("B should have depth 1", 1, B.getDepth());
    assertEquals("C should have depth 1", 1, C.getDepth());
    assertEquals("D should have depth 2", 2, D.getDepth());
    assertEquals("E should have depth 2", 2, E.getDepth());
    assertEquals("F should have depth 3", 3, F.getDepth());
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testDeepestCommonDep
  public void testDeepestCommonDep() {
    assertDeepestCommonDep(null, A, A);
    assertDeepestCommonDep(null, A, B);
    assertDeepestCommonDep(null, A, C);
    assertDeepestCommonDep(null, A, D);
    assertDeepestCommonDep(null, A, E);
    assertDeepestCommonDep(null, A, F);
    assertDeepestCommonDep(A, B, B);
    assertDeepestCommonDep(A, B, C);
    assertDeepestCommonDep(A, B, D);
    assertDeepestCommonDep(A, B, E);
    assertDeepestCommonDep(A, B, F);
    assertDeepestCommonDep(A, C, C);
    assertDeepestCommonDep(A, C, D);
    assertDeepestCommonDep(A, C, E);
    assertDeepestCommonDep(A, C, F);
    assertDeepestCommonDep(B, D, D);
    assertDeepestCommonDep(B, D, E);
    assertDeepestCommonDep(B, D, F);
    assertDeepestCommonDep(C, E, E);
    assertDeepestCommonDep(C, E, F);
    assertDeepestCommonDep(E, F, F);
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testDeepestCommonDepInclusive
  public void testDeepestCommonDepInclusive() {
    assertDeepestCommonDepInclusive(A, A, A);
    assertDeepestCommonDepInclusive(A, A, B);
    assertDeepestCommonDepInclusive(A, A, C);
    assertDeepestCommonDepInclusive(A, A, D);
    assertDeepestCommonDepInclusive(A, A, E);
    assertDeepestCommonDepInclusive(A, A, F);
    assertDeepestCommonDepInclusive(B, B, B);
    assertDeepestCommonDepInclusive(A, B, C);
    assertDeepestCommonDepInclusive(B, B, D);
    assertDeepestCommonDepInclusive(B, B, E);
    assertDeepestCommonDepInclusive(B, B, F);
    assertDeepestCommonDepInclusive(C, C, C);
    assertDeepestCommonDepInclusive(A, C, D);
    assertDeepestCommonDepInclusive(C, C, E);
    assertDeepestCommonDepInclusive(C, C, F);
    assertDeepestCommonDepInclusive(D, D, D);
    assertDeepestCommonDepInclusive(B, D, E);
    assertDeepestCommonDepInclusive(B, D, F);
    assertDeepestCommonDepInclusive(E, E, E);
    assertDeepestCommonDepInclusive(E, E, F);
    assertDeepestCommonDepInclusive(F, F, F);
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testGetTransitiveDepsDeepestFirst
  public void testGetTransitiveDepsDeepestFirst() {
    assertTransitiveDepsDeepestFirst(A);
    assertTransitiveDepsDeepestFirst(B, A);
    assertTransitiveDepsDeepestFirst(C, A);
    assertTransitiveDepsDeepestFirst(D, B, A);
    assertTransitiveDepsDeepestFirst(E, C, B, A);
    assertTransitiveDepsDeepestFirst(F, E, C, B, A);
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testCoalesceDuplicateFiles
  public void testCoalesceDuplicateFiles() {
    A.add(SourceFile.fromCode("a.js", ""));

    B.add(SourceFile.fromCode("a.js", ""));
    B.add(SourceFile.fromCode("b.js", ""));

    C.add(SourceFile.fromCode("b.js", ""));
    C.add(SourceFile.fromCode("c.js", ""));

    E.add(SourceFile.fromCode("c.js", ""));
    E.add(SourceFile.fromCode("d.js", ""));

    graph.coalesceDuplicateFiles();

    assertEquals(2, A.getInputs().size());
    assertEquals("a.js", A.getInputs().get(0).getName());
    assertEquals("b.js", A.getInputs().get(1).getName());
    assertEquals(0, B.getInputs().size());
    assertEquals(1, C.getInputs().size());
    assertEquals("c.js", C.getInputs().get(0).getName());
    assertEquals(1, E.getInputs().size());
    assertEquals("d.js", E.getInputs().get(0).getName());
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testManageDependencies1
  public void testManageDependencies1() throws Exception {
    List<CompilerInput> inputs = setUpManageDependenciesTest();
    List<CompilerInput> results = graph.manageDependencies(
        ImmutableList.<String>of(), inputs);

    assertInputs(A, "a1", "a3");
    assertInputs(B, "a2", "b2");
    assertInputs(C); 
    assertInputs(E, "c1", "e1", "e2");

    assertEquals(
        Lists.newArrayList("a1", "a3", "a2", "b2", "c1", "e1", "e2"),
        sourceNames(results));
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testManageDependencies2
  public void testManageDependencies2() throws Exception {
    List<CompilerInput> inputs = setUpManageDependenciesTest();
    List<CompilerInput> results = graph.manageDependencies(
        ImmutableList.<String>of("c2"), inputs);

    assertInputs(A, "a1", "a3");
    assertInputs(B, "a2", "b2");
    assertInputs(C, "c1", "c2");
    assertInputs(E, "e1", "e2");

    assertEquals(
        Lists.newArrayList("a1", "a3", "a2", "b2", "c1", "c2", "e1", "e2"),
        sourceNames(results));
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testManageDependencies3
  public void testManageDependencies3() throws Exception {
    List<CompilerInput> inputs = setUpManageDependenciesTest();
    DependencyOptions depOptions = new DependencyOptions();
    depOptions.setDependencySorting(true);
    depOptions.setDependencyPruning(true);
    depOptions.setMoocherDropping(true);
    depOptions.setEntryPoints(ImmutableList.<String>of("c2"));
    List<CompilerInput> results = graph.manageDependencies(
        depOptions, inputs);

    
    
    assertInputs(A);
    assertInputs(B);
    assertInputs(C, "a1", "c1", "c2");
    assertInputs(E);

    assertEquals(
        Lists.newArrayList("a1", "c1", "c2"),
        sourceNames(results));
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testManageDependencies4
  public void testManageDependencies4() throws Exception {
    setUpManageDependenciesTest();
    DependencyOptions depOptions = new DependencyOptions();
    depOptions.setDependencySorting(true);

    List<CompilerInput> inputs = Lists.newArrayList();

    
    inputs.addAll(E.getInputs());
    inputs.addAll(B.getInputs());
    inputs.addAll(A.getInputs());
    inputs.addAll(C.getInputs());

    List<CompilerInput> results = graph.manageDependencies(
        depOptions, inputs);

    assertInputs(A, "a1", "a2", "a3");
    assertInputs(B, "b1", "b2");
    assertInputs(C, "c1", "c2");
    assertInputs(E, "e1", "e2");

    assertEquals(
        Lists.newArrayList(
            "a1", "a2", "a3", "b1", "b2", "c1", "c2", "e1", "e2"),
        sourceNames(results));
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testNoFiles
  public void testNoFiles() throws Exception {
    DependencyOptions depOptions = new DependencyOptions();
    depOptions.setDependencySorting(true);

    List<CompilerInput> inputs = Lists.newArrayList();
    List<CompilerInput> results = graph.manageDependencies(
        depOptions, inputs);
    assertTrue(results.isEmpty());
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testToJson
  public void testToJson() throws JSONException {
    JSONArray modules = graph.toJson();
    assertEquals(6, modules.length());
    for (int i = 0; i < modules.length(); i++) {
      JSONObject m = modules.getJSONObject(i);
      assertNotNull(m.getString("name"));
      assertNotNull(m.getJSONArray("dependencies"));
      assertNotNull(m.getJSONArray("transitive-dependencies"));
      assertNotNull(m.getJSONArray("inputs"));
    }
    JSONObject m = modules.getJSONObject(3);
    assertEquals("D", m.getString("name"));
    assertEquals("[\"B\"]", m.getJSONArray("dependencies").toString());
    assertEquals(2,
        m.getJSONArray("transitive-dependencies").length());
    assertEquals("[]", m.getJSONArray("inputs").toString());
  }

// com.google.javascript.jscomp.JSModuleTest::testDependencies
  public void testDependencies() {
    assertEquals(ImmutableSet.of(), mod1.getAllDependencies());
    assertEquals(ImmutableSet.of(mod1), mod2.getAllDependencies());
    assertEquals(ImmutableSet.of(mod1), mod3.getAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod2, mod3), mod4.getAllDependencies());

    assertEquals(ImmutableSet.of(mod1), mod1.getThisAndAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod2), mod2.getThisAndAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod3), mod3.getThisAndAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod2, mod3, mod4),
                 mod4.getThisAndAllDependencies());
  }

// com.google.javascript.jscomp.JSModuleTest::testSortInputs
  public void testSortInputs() throws Exception {
    CompilerInput a = new CompilerInput(
        SourceFile.fromCode("a.js",
            "goog.require('b');goog.require('c')"));
    CompilerInput b = new CompilerInput(
        SourceFile.fromCode("b.js",
            "goog.provide('b');goog.require('d')"));
    CompilerInput c = new CompilerInput(
        SourceFile.fromCode("c.js",
            "goog.provide('c');goog.require('d')"));
    CompilerInput d = new CompilerInput(
        SourceFile.fromCode("d.js",
            "goog.provide('d')"));

    
    CompilerInput e = new CompilerInput(
        SourceFile.fromCode("e.js",
            "goog.provide('e')"));
    CompilerInput f = new CompilerInput(
        SourceFile.fromCode("f.js",
            "goog.provide('f')"));

    assertSortedInputs(
        ImmutableList.of(d, b, c, a),
        ImmutableList.of(a, b, c, d));
    assertSortedInputs(
        ImmutableList.of(d, b, c, a),
        ImmutableList.of(d, b, c, a));
    assertSortedInputs(
        ImmutableList.of(d, c, b, a),
        ImmutableList.of(d, c, b, a));
    assertSortedInputs(
        ImmutableList.of(d, b, c, a),
        ImmutableList.of(d, a, b, c));

    assertSortedInputs(
        ImmutableList.of(d, b, c, a, e, f),
        ImmutableList.of(a, b, c, d, e, f));
    assertSortedInputs(
        ImmutableList.of(e, f, d, b, c, a),
        ImmutableList.of(e, f, a, b, c, d));
    assertSortedInputs(
        ImmutableList.of(e, d, b, c, a, f),
        ImmutableList.of(a, b, c, e, d, f));
    assertSortedInputs(
        ImmutableList.of(e, f, d, b, c, a),
        ImmutableList.of(e, a, f, b, c, d));
  }

// com.google.javascript.jscomp.JSModuleTest::testSortJsModules
  public void testSortJsModules() throws Exception {
    
    assertEquals(ImmutableList.of(mod1, mod2, mod3, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod1, mod2, mod3, mod4))));
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod1, mod3, mod2, mod4))));

    
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod4, mod3, mod2, mod1))));
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod3, mod1, mod2, mod4))));

    
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod4, mod3, mod1, mod2))));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testSyntaxError1
  public void testSyntaxError1() {
    try {
      extractMessage("if (true) {}}");
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("JSCompiler errors\n"));
      assertTrue(e.getMessage().contains(
          "testcode:1: ERROR - Parse error. syntax error\n"));
      assertTrue(e.getMessage().contains("if (true) {}}\n"));
    }
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testSyntaxError2
  public void testSyntaxError2() {
    try {
      extractMessage("", "if (true) {}}");
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertTrue(
          e.getMessage(),
          e.getMessage().contains("JSCompiler errors\n"));
      assertTrue(
          e.getMessage(),
          e.getMessage().contains(
              "testcode:2: ERROR - Parse error. syntax error\n"));
      assertTrue(
          e.getMessage(),
          e.getMessage().contains("if (true) {}}\n"));
    }
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractNewStyleMessage1
  public void testExtractNewStyleMessage1() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .build(),
        extractMessage("var MSG_SILLY = goog.getMsg('silly test message');"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractNewStyleMessage2
  public void testExtractNewStyleMessage2() {
    
    assertEquals(
        new JsMessage.Builder("MSG_WELCOME")
            .appendStringPart("Hi ")
            .appendPlaceholderReference("userName")
            .appendStringPart("! Welcome to ")
            .appendPlaceholderReference("product")
            .appendStringPart(".")
            .setDesc("The welcome message.")
            .setIsHidden(true)
            .build(),
        extractMessage(
            "",
            "var MSG_WELCOME = goog.getMsg(",
            "    'Hi {$userName}! Welcome to {$product}.',",
            "    {userName: someUserName, product: getProductName()});"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage1
  public void testExtractOldStyleMessage1() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .setDesc("Description.")
            .build(),
        extractMessage(
            "var MSG_SILLY_HELP = 'Description.';",
            "var MSG_SILLY = 'silly test message';"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage2
  public void testExtractOldStyleMessage2() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .setDesc("Description.")
            .build(),
        extractMessage(
            "var MSG_SILLY = 'silly test message';",
            "var MSG_SILLY_HELP = 'Descrip' + 'tion.';"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage3
  public void testExtractOldStyleMessage3() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendPlaceholderReference("one")
            .appendStringPart(", ")
            .appendPlaceholderReference("two")
            .appendStringPart(", buckle my shoe")
            .build(),
        extractMessage(
            "var MSG_SILLY = function(one, two) {",
            "  return one + ', ' + two + ', buckle my shoe';",
            "};"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractMixedMessages
  public void testExtractMixedMessages() {
    
    Iterator<JsMessage> msgs = extractMessages(
        "var MSG_MONEY = function(amount) {",
        "  return 'You owe $' + amount +",
        "         ' to the credit card company.';",
        "};",
        "var MSG_TIME = goog.getMsg('You need to finish your work in ' +",
        "                           '{$duration} hours.', {'duration': d});",
        "var MSG_NAG = 'Clean your room.\\n\\nWash your clothes.';",
        "var MSG_NAG_HELP = 'Just some ' +",
        "                   'nags.';").iterator();

    assertEquals(
        new JsMessage.Builder("MSG_MONEY")
            .appendStringPart("You owe $")
            .appendPlaceholderReference("amount")
            .appendStringPart(" to the credit card company.")
            .build(),
        msgs.next());
    assertEquals(
        new JsMessage.Builder("MSG_TIME")
            .appendStringPart("You need to finish your work in ")
            .appendPlaceholderReference("duration")
            .appendStringPart(" hours.")
            .build(),
        msgs.next());
    assertEquals(
        new JsMessage.Builder("MSG_NAG")
            .appendStringPart("Clean your room.\n\nWash your clothes.")
            .setDesc("Just some nags.")
            .build(),
        msgs.next());
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testDuplicateUnnamedVariables
  public void testDuplicateUnnamedVariables() {
    
    
    Collection<JsMessage> msgs = extractMessages(
        "function a() {",
        "  var MSG_UNNAMED_2 = goog.getMsg('foo');",
        "}",
        "function b() {",
        "  var MSG_UNNAMED_2 = goog.getMsg('bar');",
        "}");

    assertEquals(2, msgs.size());
    final Iterator<JsMessage> iter = msgs.iterator();
    assertEquals("foo", iter.next().toString());
    assertEquals("bar", iter.next().toString());
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testMeaningAnnotation
  public void testMeaningAnnotation() {
    List<JsMessage> msgs = Lists.newArrayList(
        extractMessages(
            "var MSG_UNNAMED_1 = goog.getMsg('foo');",
            "var MSG_UNNAMED_2 = goog.getMsg('foo');"));
    assertEquals(2, msgs.size());
    assertTrue(msgs.get(0).getId().equals(msgs.get(1).getId()));
    assertEquals(msgs.get(0), msgs.get(1));

    msgs = Lists.newArrayList(
        extractMessages(
            "var MSG_UNNAMED_1 = goog.getMsg('foo');",
            " var MSG_UNNAMED_2 = goog.getMsg('foo');"));
    assertEquals(2, msgs.size());
    assertFalse(msgs.get(0).getId().equals(msgs.get(1).getId()));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testJsMessageOnVar
  public void testJsMessageOnVar() {
    extractMessagesSafely(
        " var MSG_HELLO = goog.getMsg('a')");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_HELLO", msg.getKey());
    assertEquals("Hello", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testJsMessageOnProperty
  public void testJsMessageOnProperty() {
    extractMessagesSafely(" " +
        "pint.sub.MSG_MENU_MARK_AS_UNREAD = goog.getMsg('a')");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_MENU_MARK_AS_UNREAD", msg.getKey());
    assertEquals("a", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testOrphanedJsMessage
  public void testOrphanedJsMessage() {
    extractMessagesSafely("goog.getMsg('a')");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(0, messages.size());

    JSError warn = compiler.getWarnings()[0];
    assertEquals(JsMessageVisitor.MESSAGE_NODE_IS_ORPHANED, warn.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMessageWithoutDescription
  public void testMessageWithoutDescription() {
    extractMessagesSafely("var MSG_HELLO = goog.getMsg('a')");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_HELLO", msg.getKey());

    assertEquals(JsMessageVisitor.MESSAGE_HAS_NO_DESCRIPTION,
        compiler.getWarnings()[0].getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIncorrectMessageReporting
  public void testIncorrectMessageReporting() {
    extractMessages("var MSG_HELLO = goog.getMsg('a' + + 'b')");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, messages.size());

    JSError mailformedTreeError = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED,
        mailformedTreeError.getType());
    assertEquals("Message parse tree malformed. "
        + "STRING or ADD node expected; found: POS",
        mailformedTreeError.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyMessage
  public void testEmptyMessage() {
    
    extractMessagesSafely("var MSG_EMPTY = '';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_EMPTY", msg.getKey());
    assertEquals("", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testConcatOfStrings
  public void testConcatOfStrings() {
    extractMessagesSafely("var MSG_NOTEMPTY = 'aa' + 'bbb' \n + ' ccc';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_NOTEMPTY", msg.getKey());
    assertEquals("aabbb ccc", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyFormatDescription
  public void testLegacyFormatDescription() {
    extractMessagesSafely("var MSG_SILLY = 'silly test message';\n"
        + "var MSG_SILLY_HELP = 'help text';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("silly test message", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyFormatParametizedFunction
  public void testLegacyFormatParametizedFunction() {
    extractMessagesSafely("var MSG_SILLY = function(one, two) {"
        + "  return one + ', ' + two + ', buckle my shoe';"
        + "};");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals(null, msg.getDesc());
    assertEquals("{$one}, {$two}, buckle my shoe", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyMessageWithDescAnnotation
  public void testLegacyMessageWithDescAnnotation() {
    
    
    extractMessagesSafely(
        " var MSG_A = 'The Message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_A", msg.getKey());
    assertEquals("The Message", msg.toString());
    assertEquals("The description", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyMessageWithDescAnnotationAndHelpVar
  public void testLegacyMessageWithDescAnnotationAndHelpVar() {
    
    
    extractMessagesSafely(
        "var MSG_A_HELP = 'This is a help var';\n" +
        " var MSG_A = 'The Message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_A", msg.getKey());
    assertEquals("The Message", msg.toString());
    assertEquals("The description in @desc", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureMessageWithHelpPostfix
  public void testClosureMessageWithHelpPostfix() {
    extractMessagesSafely("\n"
        + "var MSG_FOO_HELP = goog.getMsg('Help!');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_FOO_HELP", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("Help!", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureMessageWithoutGoogGetmsg
  public void testClosureMessageWithoutGoogGetmsg() {
    allowLegacyMessages = false;

    extractMessages("var MSG_FOO_HELP = 'I am a bad message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_NOT_INITIALIZED_USING_NEW_SYNTAX,
        error.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureFormatParametizedFunction
  public void testClosureFormatParametizedFunction() {
    extractMessagesSafely(""
        + "var MSG_SILLY = goog.getMsg('{$adjective} ' + 'message', "
        + "{'adjective': 'silly'});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("{$adjective} message", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testHugeMessage
  public void testHugeMessage() {
    extractMessagesSafely("" +
        "var MSG_HUGE = goog.getMsg(" +
        "    '{$startLink_1}Google{$endLink}' +" +
        "    '{$startLink_2}blah{$endLink}{$boo}{$foo_001}{$boo}' +" +
        "    '{$foo_002}{$xxx_001}{$image}{$image_001}{$xxx_002}'," +
        "    {'startLink_1': '<a href=http://www.google.com/>'," +
        "     'endLink': '</a>'," +
        "     'startLink_2': '<a href=\"' + opt_data.url + '\">'," +
        "     'boo': opt_data.boo," +
        "     'foo_001': opt_data.foo," +
        "     'foo_002': opt_data.boo.foo," +
        "     'xxx_001': opt_data.boo + opt_data.foo," +
        "     'image': htmlTag7," +
        "     'image_001': opt_data.image," +
        "     'xxx_002': foo.callWithOnlyTopLevelKeys(" +
        "         bogusFn, opt_data, null, 'bogusKey1'," +
        "         opt_data.moo, 'bogusKey2', param10)});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_HUGE", msg.getKey());
    assertEquals("A message with lots of stuff.", msg.getDesc());
    assertTrue(msg.isHidden());
    assertEquals("{$startLink_1}Google{$endLink}{$startLink_2}blah{$endLink}" +
        "{$boo}{$foo_001}{$boo}{$foo_002}{$xxx_001}{$image}" +
        "{$image_001}{$xxx_002}", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnnamedGoogleMessage
  public void testUnnamedGoogleMessage() {
    extractMessagesSafely("var MSG_UNNAMED_2 = goog.getMsg('Hullo');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals(null, msg.getDesc());
    assertEquals("MSG_16LJMYKCXT84X", msg.getKey());
    assertEquals("MSG_16LJMYKCXT84X", msg.getId());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyTextMessage
  public void testEmptyTextMessage() {
    extractMessagesSafely(" var MSG_FOO = goog.getMsg('');");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    assertEquals("Message value of MSG_FOO is just an empty string. "
        + "Empty messages are forbidden.",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyTextComplexMessage
  public void testEmptyTextComplexMessage() {
    extractMessagesSafely(" var MSG_BAR = goog.getMsg("
        + "'' + '' + ''     + ''\n+'');");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    assertEquals("Message value of MSG_BAR is just an empty string. "
        + "Empty messages are forbidden.",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMessageIsNoUnnamed
  public void testMessageIsNoUnnamed() {
    extractMessagesSafely("var MSG_UNNAMED_ITEM = goog.getMsg('Hullo');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_UNNAMED_ITEM", msg.getKey());
    assertFalse(msg.isHidden());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMsgVarWithoutAssignment
  public void testMsgVarWithoutAssignment() {
    extractMessages("var MSG_SILLY;");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_HAS_NO_VALUE, error.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testRegularVarWithoutAssignment
  public void testRegularVarWithoutAssignment() {
    extractMessagesSafely("var SILLY;");

    assertTrue(messages.isEmpty());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMsgVarWithIncorrectRightSide
  public void testMsgVarWithIncorrectRightSide() {
    extractMessages("var MSG_SILLY = 0;");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. Cannot parse value of "
        + "message MSG_SILLY", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIncorrectMessage
  public void testIncorrectMessage() {
    extractMessages("DP_DatePicker.MSG_DATE_SELECTION = {};");

    assertEquals(0, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. "+
                 "Message must be initialized using goog.getMsg function.",
                 error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnrecognizedFunction
  public void testUnrecognizedFunction() {
    allowLegacyMessages = false;
    extractMessages("DP_DatePicker.MSG_DATE_SELECTION = somefunc('a')");

    assertEquals(0, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. "+
                 "Message initialized using unrecognized function. " +
                 "Please use goog.getMsg() instead.",
                 error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testExtractPropertyMessage
  public void testExtractPropertyMessage() {
    extractMessagesSafely(""
        + "a.b.MSG_SILLY = goog.getMsg(\n"
        + "    '{$adjective} ' + '{$someNoun}',\n"
        + "    {'adjective': adj, 'someNoun': noun});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("{$adjective} {$someNoun}", msg.toString());
    assertEquals("A message that demonstrates placeholders", msg.getDesc());
    assertTrue(msg.isHidden());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testAlmostButNotExternalMessage
  public void testAlmostButNotExternalMessage() {
    extractMessagesSafely(
        " var MSG_EXTERNAL = goog.getMsg('External');");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());
    assertFalse(messages.get(0).isExternal());
    assertEquals("MSG_EXTERNAL", messages.get(0).getKey());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testExternalMessage
  public void testExternalMessage() {
    extractMessagesSafely("var MSG_EXTERNAL_111 = goog.getMsg('Hello World');");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());
    assertTrue(messages.get(0).isExternal());
    assertEquals("111", messages.get(0).getId());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameStrict
  public void testIsValidMessageNameStrict() {
    JsMessageVisitor visitor = new DummyJsVisitor(CLOSURE);

    assertTrue(visitor.isMessageName("MSG_HELLO", true));
    assertTrue(visitor.isMessageName("MSG_", true));
    assertTrue(visitor.isMessageName("MSG_HELP", true));
    assertTrue(visitor.isMessageName("MSG_FOO_HELP", true));

    assertFalse(visitor.isMessageName("_FOO_HELP", true));
    assertFalse(visitor.isMessageName("MSGFOOP", true));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameRelax
  public void testIsValidMessageNameRelax() {
    JsMessageVisitor visitor = new DummyJsVisitor(RELAX);

    assertFalse(visitor.isMessageName("MSG_HELP", false));
    assertFalse(visitor.isMessageName("MSG_FOO_HELP", false));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameLegacy
  public void testIsValidMessageNameLegacy() {
    theseAreLegacyMessageNames(new DummyJsVisitor(RELAX));
    theseAreLegacyMessageNames(new DummyJsVisitor(LEGACY));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnexistedPlaceholders
  public void testUnexistedPlaceholders() {
    extractMessages("var MSG_FOO = goog.getMsg('{$foo}:', {});");

    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Unrecognized message "
        + "placeholder referenced: foo", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnusedReferenesAreNotOK
  public void testUnusedReferenesAreNotOK() {
    extractMessages(" "
        + "var MSG_FOO = goog.getMsg('lalala:', {foo:1});");
    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Unused message placeholder: "
        + "foo", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testDuplicatePlaceHoldersAreBad
  public void testDuplicatePlaceHoldersAreBad() {
    extractMessages("var MSG_FOO = goog.getMsg("
        + "'{$foo}:', {'foo': 1, 'foo' : 2});");

    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Duplicate placeholder "
        + "name: foo", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testDuplicatePlaceholderReferencesAreOk
  public void testDuplicatePlaceholderReferencesAreOk() {
    extractMessagesSafely("var MSG_FOO = goog.getMsg("
        + "'{$foo}:, {$foo}', {'foo': 1});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("{$foo}:, {$foo}", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testCamelcasePlaceholderNamesAreOk
  public void testCamelcasePlaceholderNamesAreOk() {
    extractMessagesSafely("var MSG_WITH_CAMELCASE = goog.getMsg("
        + "'Slide {$slideNumber}:', {'slideNumber': opt_index + 1});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_WITH_CAMELCASE", msg.getKey());
    assertEquals("Slide {$slideNumber}:", msg.toString());
    List<CharSequence> parts = msg.parts();
    assertEquals(3, parts.size());
    assertEquals("slideNumber",
        ((JsMessage.PlaceholderReference)parts.get(1)).getName());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testWithNonCamelcasePlaceholderNamesAreNotOk
  public void testWithNonCamelcasePlaceholderNamesAreNotOk() {
    extractMessages("var MSG_WITH_CAMELCASE = goog.getMsg("
        + "'Slide {$slide_number}:', {'slide_number': opt_index + 1});");

    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Placeholder name not in "
        + "lowerCamelCase: slide_number", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnquotedPlaceholdersAreOk
  public void testUnquotedPlaceholdersAreOk() {
    extractMessagesSafely(" "
        + "var MSG_FOO = goog.getMsg('foo {$unquoted}:', {unquoted: 12});");

    assertEquals(1, messages.size());
    assertEquals(0, compiler.getWarningCount());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsLowerCamelCaseWithNumericSuffixes
  public void testIsLowerCamelCaseWithNumericSuffixes() {
    assertTrue(isLowerCamelCaseWithNumericSuffixes("name"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("NAME"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("Name"));

    assertTrue(isLowerCamelCaseWithNumericSuffixes("a4Letter"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("A4_LETTER"));

    assertTrue(isLowerCamelCaseWithNumericSuffixes("startSpan_1_23"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("startSpan_1_23b"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("START_SPAN_1_23"));

    assertFalse(isLowerCamelCaseWithNumericSuffixes(""));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testToLowerCamelCaseWithNumericSuffixes
  public void testToLowerCamelCaseWithNumericSuffixes() {
    assertEquals("name", toLowerCamelCaseWithNumericSuffixes("NAME"));
    assertEquals("a4Letter", toLowerCamelCaseWithNumericSuffixes("A4_LETTER"));
    assertEquals("startSpan_1_23",
        toLowerCamelCaseWithNumericSuffixes("START_SPAN_1_23"));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testDuplicateMessageError
  public void testDuplicateMessageError() {
    extractMessages(
        "(function () { var MSG_HELLO = goog.getMsg('a')})" +
        "(function () { var MSG_HELLO = goog.getMsg('a')})");

    assertEquals(0, compiler.getWarningCount());
    assertOneError(JsMessageVisitor.MESSAGE_DUPLICATE_KEY);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testNoDuplicateErrorOnExternMessage
  public void testNoDuplicateErrorOnExternMessage() {
    extractMessagesSafely(
        "(function () { " +
        "var MSG_EXTERNAL_2 = goog.getMsg('a')})" +
        "(function () { " +
        "var MSG_EXTERNAL_2 = goog.getMsg('a')})");
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testErrorWhenUsingMsgPrefixWithFallback
  public void testErrorWhenUsingMsgPrefixWithFallback() {
    extractMessages(
        " var MSG_HELLO_1 = goog.getMsg('hello');\n" +
        " var MSG_HELLO_2 = goog.getMsg('hello');\n" +
        " " +
        "var MSG_HELLO_3 = goog.getMsgWithFallback(MSG_HELLO_1, MSG_HELLO_2);");
    assertOneError(JsMessageVisitor.MESSAGE_TREE_MALFORMED);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testOptimize
  public void testOptimize() {
    assertEquals(localEntry, localEntry.optimize());

    FlowScope child = localEntry.createChildFlowScope();
    assertEquals(localEntry, child.optimize());

    child.inferSlotType("localB", NUMBER_TYPE);
    assertEquals(child, child.optimize());
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testJoin1
  public void testJoin1() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localB", NUMBER_TYPE);

    FlowScope childAB = childA.createChildFlowScope();
    childAB.inferSlotType("localB", STRING_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childB.inferSlotType("localB", BOOLEAN_TYPE);

    assertTypeEquals(STRING_TYPE, childAB.getSlot("localB").getType());
    assertTypeEquals(BOOLEAN_TYPE, childB.getSlot("localB").getType());
    assertNull(childB.getSlot("localA").getType());

    FlowScope joined = join(childB, childAB);
    assertTypeEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localB").getType());
    assertNull(joined.getSlot("localA").getType());

    joined = join(childAB, childB);
    assertTypeEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localB").getType());
    assertNull(joined.getSlot("localA").getType());

    assertEquals("Join should be symmetric",
        join(childB, childAB), join(childAB, childB));
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testJoin2
  public void testJoin2() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localA", STRING_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childB.inferSlotType("globalB", BOOLEAN_TYPE);

    assertTypeEquals(STRING_TYPE, childA.getSlot("localA").getType());
    assertTypeEquals(BOOLEAN_TYPE, childB.getSlot("globalB").getType());
    assertNull(childB.getSlot("localB").getType());

    FlowScope joined = join(childB, childA);
    assertTypeEquals(STRING_TYPE, joined.getSlot("localA").getType());
    assertTypeEquals(BOOLEAN_TYPE, joined.getSlot("globalB").getType());

    joined = join(childA, childB);
    assertTypeEquals(STRING_TYPE, joined.getSlot("localA").getType());
    assertTypeEquals(BOOLEAN_TYPE, joined.getSlot("globalB").getType());

    assertEquals("Join should be symmetric",
        join(childB, childA), join(childA, childB));
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testJoin3
  public void testJoin3() {
    localScope.declare("localC", null, STRING_TYPE, null);
    localScope.declare("localD", null, STRING_TYPE, null);

    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localC", NUMBER_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childA.inferSlotType("localD", BOOLEAN_TYPE);

    FlowScope joined = join(childB, childA);
    assertTypeEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        joined.getSlot("localC").getType());
    assertTypeEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localD").getType());

    joined = join(childA, childB);
    assertTypeEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        joined.getSlot("localC").getType());
    assertTypeEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localD").getType());

    assertEquals("Join should be symmetric",
        join(childB, childA), join(childA, childB));
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testLongChain1
  public void testLongChain1() {
    FlowScope chainA = localEntry.createChildFlowScope();
    FlowScope chainB = localEntry.createChildFlowScope();
    for (int i = 0; i < LONG_CHAIN_LENGTH; i++) {
      localScope.declare("local" + i, null, null, null);
      chainA.inferSlotType("local" + i,
          i % 2 == 0 ? NUMBER_TYPE : BOOLEAN_TYPE);
      chainB.inferSlotType("local" + i,
          i % 3 == 0 ? STRING_TYPE : BOOLEAN_TYPE);

      chainA = chainA.createChildFlowScope();
      chainB = chainB.createChildFlowScope();
    }

    verifyLongChains(chainA, chainB);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testLongChain2
  public void testLongChain2() {
    FlowScope chainA = localEntry.createChildFlowScope();
    FlowScope chainB = localEntry.createChildFlowScope();
    for (int i = 0; i < LONG_CHAIN_LENGTH * 7; i++) {
      localScope.declare("local" + i, null, null, null);
      chainA.inferSlotType("local" + i,
          i % 2 == 0 ? NUMBER_TYPE : BOOLEAN_TYPE);
      chainB.inferSlotType("local" + i,
          i % 3 == 0 ? STRING_TYPE : BOOLEAN_TYPE);

      if (i % 7 == 0) {
        chainA = chainA.createChildFlowScope();
        chainB = chainB.createChildFlowScope();
      }
    }

    verifyLongChains(chainA, chainB);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testLongChain3
  public void testLongChain3() {
    FlowScope chainA = localEntry.createChildFlowScope();
    FlowScope chainB = localEntry.createChildFlowScope();
    for (int i = 0; i < LONG_CHAIN_LENGTH * 7; i++) {
      if (i % 7 == 0) {
        int j = i / 7;
        localScope.declare("local" + j, null, null, null);
        chainA.inferSlotType("local" + j,
            j % 2 == 0 ? NUMBER_TYPE : BOOLEAN_TYPE);
        chainB.inferSlotType("local" + j,
            j % 3 == 0 ? STRING_TYPE : BOOLEAN_TYPE);
      }

      chainA = chainA.createChildFlowScope();
      chainB = chainB.createChildFlowScope();
    }

    verifyLongChains(chainA, chainB);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testFindUniqueSlot
  public void testFindUniqueSlot() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localB", NUMBER_TYPE);

    FlowScope childAB = childA.createChildFlowScope();
    childAB.inferSlotType("localB", STRING_TYPE);

    FlowScope childABC = childAB.createChildFlowScope();
    childABC.inferSlotType("localA", BOOLEAN_TYPE);

    assertNull(childABC.findUniqueRefinedSlot(childABC));
    assertTypeEquals(BOOLEAN_TYPE,
        childABC.findUniqueRefinedSlot(childAB).getType());
    assertNull(childABC.findUniqueRefinedSlot(childA));
    assertNull(childABC.findUniqueRefinedSlot(localEntry));

    assertTypeEquals(STRING_TYPE,
        childAB.findUniqueRefinedSlot(childA).getType());
    assertTypeEquals(STRING_TYPE,
        childAB.findUniqueRefinedSlot(localEntry).getType());

    assertTypeEquals(NUMBER_TYPE,
        childA.findUniqueRefinedSlot(localEntry).getType());
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testDiffer1
  public void testDiffer1() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localB", NUMBER_TYPE);

    FlowScope childAB = childA.createChildFlowScope();
    childAB.inferSlotType("localB", STRING_TYPE);

    FlowScope childABC = childAB.createChildFlowScope();
    childABC.inferSlotType("localA", BOOLEAN_TYPE);

    FlowScope childB = childAB.createChildFlowScope();
    childB.inferSlotType("localB", STRING_TYPE);

    FlowScope childBC = childB.createChildFlowScope();
    childBC.inferSlotType("localA", NO_TYPE);

    assertScopesSame(childAB, childB);
    assertScopesDiffer(childABC, childBC);

    assertScopesDiffer(childABC, childB);
    assertScopesDiffer(childAB, childBC);

    assertScopesDiffer(childA, childAB);
    assertScopesDiffer(childA, childABC);
    assertScopesDiffer(childA, childB);
    assertScopesDiffer(childA, childBC);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testDiffer2
  public void testDiffer2() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localA", NUMBER_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childB.inferSlotType("localA", NO_TYPE);

    assertScopesDiffer(childA, childB);
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testStraightLine
  public void testStraightLine() {
    
    assertNotLiveBeforeX("X:var a;", "a");
    assertNotLiveAfterX("X:var a;", "a");
    assertNotLiveAfterX("X:var a=1;", "a");
    assertLiveAfterX("X:var a=1; a()", "a");
    assertNotLiveBeforeX("X:var a=1; a()", "a");
    assertLiveBeforeX("var a;X:a;", "a");
    assertLiveBeforeX("var a;X:a=a+1;", "a");
    assertLiveBeforeX("var a;X:a+=1;", "a");
    assertLiveBeforeX("var a;X:a++;", "a");
    assertNotLiveAfterX("var a,b;X:b();", "a");
    assertNotLiveBeforeX("var a,b;X:b();", "a");
    assertLiveBeforeX("var a,b;X:b(a);", "a");
    assertLiveBeforeX("var a,b;X:b(1,2,3,b(a + 1));", "a");
    assertNotLiveBeforeX("var a,b;X:a=1;b(a)", "a");
    assertNotLiveAfterX("var a,b;X:b(a);b()", "a");
    assertLiveBeforeX("var a,b;X:b();b=1;a()", "b");
    assertLiveAfterX("X:a();var a;a()", "a");
    assertNotLiveAfterX("X:a();var a=1;a()", "a");
    assertLiveBeforeX("var a,b;X:a,b=1", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testProperties
  public void testProperties() {
    
    assertLiveBeforeX("var a,b;X:a.P;", "a");

    
    assertLiveBeforeX("var a,b;X:a.P=1;b()", "a");
    assertLiveBeforeX("var a,b;X:a.P.Q=1;b()", "a");

    
    assertNotLiveAfterX("var a,b;X:b.P.Q.a=1;", "a");

    assertLiveBeforeX("var a,b;X:b.P.Q=a;", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testConditions
  public void testConditions() {
    
    assertLiveBeforeX("var a,b;X:if(a){}", "a");
    assertLiveBeforeX("var a,b;X:if(a||b) {}", "a");
    assertLiveBeforeX("var a,b;X:if(b||a) {}", "a");
    assertLiveBeforeX("var a,b;X:if(b||b(a)) {}", "a");
    assertNotLiveAfterX("var a,b;X:b();if(a) {}", "b");

    
    assertNotLiveAfterX("var a,b;X:a();if(a=b){}a()", "a");
    assertNotLiveAfterX("var a,b;X:a();while(a=b){}a()", "a");

    
    assertNotLiveAfterX("var a,b;X:a();if((a=b)&&b){}a()", "a");
    assertNotLiveAfterX("var a,b;X:a();while((a=b)&&b){}a()", "a");
    assertLiveBeforeX("var a,b;a();X:if(b&&(a=b)){}a()", "a"); 
    assertLiveBeforeX("var a,b;a();X:if(a&&(a=b)){}a()", "a");
    assertLiveBeforeX("var a,b;a();X:while(b&&(a=b)){}a()", "a");
    assertLiveBeforeX("var a,b;a();X:while(a&&(a=b)){}a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testArrays
  public void testArrays() {
    assertLiveBeforeX("var a;X:a[1]", "a");
    assertLiveBeforeX("var a,b;X:b[a]", "a");
    assertLiveBeforeX("var a,b;X:b[1,2,3,4,b(a)]", "a");
    assertLiveBeforeX("var a,b;X:b=[a,'a']", "a");
    assertNotLiveBeforeX("var a,b;X:a=[];b(a)", "a");

    
    assertLiveBeforeX("var a;X:a[1]=1", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testTwoPaths
  public void testTwoPaths() {
    
    assertLiveBeforeX("var a,b;X:if(b){b(a)}else{b(a)};", "a");

    
    assertLiveBeforeX("var a,b;X:if(b){b(b)}else{b(a)};", "a");
    assertLiveBeforeX("var a,b;X:if(b){b(a)}else{b(b)};", "a");

    
    assertNotLiveAfterX("var a,b;X:if(b){b(b)}else{b(b)};", "a");

    
    assertLiveBeforeX("var a,b;X:if(b){b(b)}else{b(b)}a();", "a");

    
    assertLiveBeforeX("var a;X:while(param1){a()};", "a");
    assertLiveBeforeX("var a;X:while(param1){a=1};a()", "a");

    
    assertLiveBeforeX("var a;X:if(param1){a()};", "a");
    assertLiveBeforeX("var a;X:if(param1){a=1};a()", "a");

    
    
    assertNotLiveAfterX("X:var a;do{a=1}while(param1);a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testThreePaths
  public void testThreePaths() {
    assertLiveBeforeX("var a;X:if(1){}else if(2){}else{a()};", "a");
    assertLiveBeforeX("var a;X:if(1){}else if(2){a()}else{};", "a");
    assertLiveBeforeX("var a;X:if(1){a()}else if(2){}else{};", "a");
    assertLiveBeforeX("var a;X:if(1){}else if(2){}else{};a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testHooks
  public void testHooks() {
    assertLiveBeforeX("var a;X:1?a=1:1;a()", "a");

    
    
    
    assertLiveBeforeX("var a,b;X:b=1?a:2", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testForLoops
  public void testForLoops() {
    
    assertNotLiveBeforeX("var a,b;for(a=0;a<9;a++){b(a)};X:b", "a");
    assertNotLiveBeforeX("var a,b;for(a in b){a()};X:b", "a");
    assertNotLiveBeforeX("var a,b;for(a in b){a()};X:a", "b");
    assertLiveBeforeX("var b;for(var a in b){X:a()};", "a");

    
    assertLiveBeforeX("var a,b;for(a=0;a<9;a++){X:1}", "a");
    assertLiveAfterX("var a,b;for(a in b){X:b};", "a");
    
    assertLiveBeforeX("var a,b; X:for(a in b){ }", "a");

    
    
    

    
    assertLiveBeforeX("var a,b;X:a();b();for(a in b){a()};", "a");

    
    assertLiveBeforeX("var a,b;X:b;for(b=a;;){};", "a");
    assertNotLiveBeforeX("var a,b;X:a;for(b=a;;){b()};b();", "b");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testNestedLoops
  public void testNestedLoops() {
    assertLiveBeforeX("var a;X:while(1){while(1){a()}}", "a");
    assertLiveBeforeX("var a;X:while(1){while(1){while(1){a()}}}", "a");
    assertLiveBeforeX("var a;X:while(1){while(1){a()};a=1}", "a");
    assertLiveAfterX("var a;while(1){while(1){a()};X:a=1;}", "a");
    assertLiveAfterX("var a;while(1){X:a=1;while(1){a()}}", "a");
    assertNotLiveBeforeX(
        "var a;X:1;do{do{do{a=1;}while(1)}while(1)}while(1);a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testSwitches
  public void testSwitches() {
    assertLiveBeforeX("var a,b;X:switch(a){}", "a");
    assertLiveBeforeX("var a,b;X:switch(b){case(a):break;}", "a");
    assertLiveBeforeX("var a,b;X:switch(b){case(b):case(a):break;}", "a");
    assertNotLiveBeforeX(
        "var a,b;X:switch(b){case 1:a=1;break;default:a=2;break};a()", "a");

    assertLiveBeforeX("var a,b;X:switch(b){default:a();break;}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testAssignAndReadInCondition
  public void testAssignAndReadInCondition() {
    
    
    
    assertLiveBeforeX("var a, b; X: if ((a = this) && (b = a)) {}", "a");
    assertNotLiveBeforeX("var a, b; X: a = 1, b = 1;", "a");
    assertNotLiveBeforeX("var a; X: a = 1, a = 1;", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testParam
  public void testParam() {
    
    assertNotLiveAfterX("var a;X:a()", "param1");
    assertLiveBeforeX("var a;X:a(param1)", "param1");
    assertNotLiveAfterX("var a;X:a();a(param2)", "param1");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testExpressionInForIn
  public void testExpressionInForIn() {
    assertLiveBeforeX("var a = [0]; X:for (a[1] in foo) { }", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testArgumentsArray
  public void testArgumentsArray() {
    
    
    assertEscaped("arguments[0]", "param1");
    assertEscaped("arguments[0]", "param2");
    assertEscaped("var args = arguments", "param1");
    assertEscaped("var args = arguments", "param2");
    assertNotEscaped("arguments = []", "param1");
    assertNotEscaped("arguments = []", "param2");
    assertEscaped("arguments[0] = 1", "param1");
    assertEscaped("arguments[0] = 1", "param2");
    assertEscaped("arguments[arguments[0]] = 1", "param1");
    assertEscaped("arguments[arguments[0]] = 1", "param2");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testTryCatchFinally
  public void testTryCatchFinally() {
    assertLiveAfterX("var a; try {X:a=1} finally {a}", "a");
    assertLiveAfterX("var a; try {a()} catch(e) {X:a=1} finally {a}", "a");
    
    
    assertNotLiveAfterX("var a = 1; try {" +
        "try {a()} catch(e) {X:1} } catch(E) {a}", "a");
    assertLiveAfterX("var a; while(1) { try {X:a=1;break} finally {a}}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testForInAssignment
  public void testForInAssignment() {
    assertLiveBeforeX("var a,b; for (var y in a = b) { X:a[y] }", "a");
    
    assertNotLiveBeforeX("var a,b; for (var y in a = b) { X:a[y] }", "b");
    assertLiveBeforeX("var a,b; for (var y in a = b) { X:a[y] }", "y");
    assertLiveAfterX("var a,b; for (var y in a = b) { a[y]; X: y();}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testExceptionThrowingAssignments
  public void testExceptionThrowingAssignments() {
    assertLiveBeforeX("try{var a; X:a=foo();a} catch(e) {e()}", "a");
    assertLiveBeforeX("try{X:var a=foo();a} catch(e) {e()}", "a");
    assertLiveBeforeX("try{X:var a=foo()} catch(e) {e(a)}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testInnerFunctions
  public void testInnerFunctions() {
    assertLiveBeforeX("function a() {}; X: a()", "a");
    assertNotLiveBeforeX("X: function a() {}", "a");
    assertLiveBeforeX("a = function(){}; function a() {}; X: a()", "a");
    
    
    assertLiveAfterX("X: a = function(){}; function a() {}; a()", "a");
    assertNotLiveBeforeX("X: a = function(){}; function a() {}; a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testEscaped
  public void testEscaped() {
    assertEscaped("var a;function b(){a()}", "a");
    assertEscaped("var a;function b(){param1()}", "param1");
    assertEscaped("var a;function b(){function c(){a()}}", "a");
    assertEscaped("var a;function b(){param1.x = function() {a()}}", "a");
    assertEscaped("try{} catch(e){}", "e");
    assertNotEscaped("var a;function b(){var c; c()}", "c");
    assertNotEscaped("var a;function f(){function b(){var c;c()}}", "c");
    assertNotEscaped("var a;function b(){};a()", "a");
    assertNotEscaped("var a;function f(){function b(){}}a()", "a");
    assertNotEscaped("var a;function b(){var a;a()};a()", "a");

    
    assertEscaped("var _x", "_x");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testEscapedLiveness
  public void testEscapedLiveness() {
    assertNotLiveBeforeX("var a;X:a();function b(){a()}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testBug1449316
  public void testBug1449316() {
    assertLiveBeforeX("try {var x=[]; X:var y=x[0]} finally {foo()}", "x");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInitialTypingScope
  public void testInitialTypingScope() {
    Scope s = new TypedScopeCreator(compiler,
        CodingConventions.getDefault()).createInitialScope(
            new Node(Token.BLOCK));

    assertTypeEquals(ARRAY_FUNCTION_TYPE, s.getVar("Array").getType());
    assertTypeEquals(BOOLEAN_OBJECT_FUNCTION_TYPE,
        s.getVar("Boolean").getType());
    assertTypeEquals(DATE_FUNCTION_TYPE, s.getVar("Date").getType());
    assertTypeEquals(ERROR_FUNCTION_TYPE, s.getVar("Error").getType());
    assertTypeEquals(EVAL_ERROR_FUNCTION_TYPE,
        s.getVar("EvalError").getType());
    assertTypeEquals(NUMBER_OBJECT_FUNCTION_TYPE,
        s.getVar("Number").getType());
    assertTypeEquals(OBJECT_FUNCTION_TYPE, s.getVar("Object").getType());
    assertTypeEquals(RANGE_ERROR_FUNCTION_TYPE,
        s.getVar("RangeError").getType());
    assertTypeEquals(REFERENCE_ERROR_FUNCTION_TYPE,
        s.getVar("ReferenceError").getType());
    assertTypeEquals(REGEXP_FUNCTION_TYPE, s.getVar("RegExp").getType());
    assertTypeEquals(STRING_OBJECT_FUNCTION_TYPE,
        s.getVar("String").getType());
    assertTypeEquals(SYNTAX_ERROR_FUNCTION_TYPE,
        s.getVar("SyntaxError").getType());
    assertTypeEquals(TYPE_ERROR_FUNCTION_TYPE,
        s.getVar("TypeError").getType());
    assertTypeEquals(URI_ERROR_FUNCTION_TYPE,
        s.getVar("URIError").getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck1
  public void testTypeCheck1() throws Exception {
    testTypes("function foo(){ if (foo()) return; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck2
  public void testTypeCheck2() throws Exception {
    testTypes("function foo(){ var x=foo(); x--; }",
        "increment/decrement\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck4
  public void testTypeCheck4() throws Exception {
    testTypes("function foo(){ !foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck5
  public void testTypeCheck5() throws Exception {
    testTypes("function foo(){ var a = +foo(); }",
        "sign operator\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck6
  public void testTypeCheck6() throws Exception {
    testTypes(
        "function foo(){" +
        "var a;if (a == foo())return;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck8
  public void testTypeCheck8() throws Exception {
    testTypes("function foo(){do {} while (foo());}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck9
  public void testTypeCheck9() throws Exception {
    testTypes("function foo(){while (foo());}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck10
  public void testTypeCheck10() throws Exception {
    testTypes("function foo(){for (;foo(););}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck11
  public void testTypeCheck11() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a = b;",
        "assignment\n" +
        "found   : String\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck12
  public void testTypeCheck12() throws Exception {
    testTypes("function foo(){var a = 3^foo();}",
        "bad right operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck13
  public void testTypeCheck13() throws Exception {
    testTypes("var i; i=/xx/;",
        "assignment\n" +
        "found   : RegExp\n" +
        "required: (Number|String)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck14
  public void testTypeCheck14() throws Exception {
    testTypes("function foo(opt_a){}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck15
  public void testTypeCheck15() throws Exception {
    testTypes("var x;x=null;x=10;",
        "assignment\n" +
        "found   : number\n" +
        "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck16a
  public void testTypeCheck16a() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck16b
  public void testTypeCheck16b() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck17
  public void testTypeCheck17() throws Exception {
    testTypes("\n" +
        "function a(opt_foo){\nreturn (opt_foo);\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck18
  public void testTypeCheck18() throws Exception {
    testTypes("\n function a(){return new RegExp();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck19
  public void testTypeCheck19() throws Exception {
    testTypes("\n function a(){return new Array();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck20
  public void testTypeCheck20() throws Exception {
    testTypes("\n function a(){return new Date();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckBasicDowncast
  public void testTypeCheckBasicDowncast() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckNoDowncastToNumber
  public void testTypeCheckNoDowncastToNumber() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n",
        "initializing variable\n" +
        "found   : foo\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck21
  public void testTypeCheck21() throws Exception {
    testTypes("var foo;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck22
  public void testTypeCheck22() throws Exception {
    testTypes("\nfunction foo(p){}\n" +
                  "function Element(){}\n" +
                  "var v;\n" +
                  "foo(v);\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck23
  public void testTypeCheck23() throws Exception {
    testTypes("var foo; foo = null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck24
  public void testTypeCheck24() throws Exception {
    testTypes("function MyType(){}\n" +
        "var foo; foo = null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckDefaultExterns
  public void testTypeCheckDefaultExterns() throws Exception {
    testTypes(" function f(x) {}" +
        "f([].length);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckCustomExterns
  public void testTypeCheckCustomExterns() throws Exception {
    testTypes(
        DEFAULT_EXTERNS + " Array.prototype.oogabooga;",
        " function f(x) {}" +
        "f([].oogabooga);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string", false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray1
  public void testTemplatizedArray1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray2
  public void testTemplatizedArray2() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : Array.<number>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray3
  public void testTemplatizedArray3() throws Exception {
    testTypes(" var f = function(a) { a[1] = 0; return a[0]; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray4
  public void testTemplatizedArray4() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray5
  public void testTemplatizedArray5() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray6
  public void testTemplatizedArray6() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : *\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray7
  public void testTemplatizedArray7() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedObject1
  public void testTemplatizedObject1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedObject2
  public void testTemplatizedObject2() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedObject3
  public void testTemplatizedObject3() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedObject4
  public void testTemplatizedObject4() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: E.<string>");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnionOfFunctionAndType
  public void testUnionOfFunctionAndType() throws Exception {
    testTypes(" var a;" +
        " var b = null; a = b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalParameterComparedToUndefined
  public void testOptionalParameterComparedToUndefined() throws Exception {
    testTypes("function foo(opt_a)" +
        "{if (opt_a==undefined) var b = 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalAllType
  public void testOptionalAllType() throws Exception {
    testTypes("function f(opt_x) { return opt_x }\n" +
        "var y;\n" +
        "f(y);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalUnknownNamedType
  public void testOptionalUnknownNamedType() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { return opt_x; }\n" +
        "var T = function() {};",
        "inconsistent return type\n" +
        "found   : (T|undefined)\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam
  public void testOptionalArgFunctionParam() throws Exception {
    testTypes("" +
        "function f(a) {a()};");
  }
