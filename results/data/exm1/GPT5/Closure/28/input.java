// buggy code
    void addIdentifier(String identifier) {
      add(ESTIMATED_IDENTIFIER);
    }

// relevant test
// com.google.javascript.jscomp.AliasKeywordsTest::testDontAlias
  public void testDontAlias() {
    testSame(generateCode("true", TOO_FEW_TO_ALIAS_LITERAL));
    testSame(generateCode("false", TOO_FEW_TO_ALIAS_LITERAL));
    testSame(generateCode("null", TOO_FEW_TO_ALIAS_LITERAL));
    testSame(generateCode("void 0", TOO_FEW_TO_ALIAS_LITERAL));
    testSame(generatePreProcessThrowCode(TOO_FEW_TO_ALIAS_THROW, "1"));

    
    testSame(generateCode("void 1", ENOUGH_TO_ALIAS_LITERAL));
    testSame(generateCode("void x", ENOUGH_TO_ALIAS_LITERAL));
    testSame(generateCode("void f()", ENOUGH_TO_ALIAS_LITERAL));
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testAlias
  public void testAlias() {
    test(generateCode("true", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_TRUE, ENOUGH_TO_ALIAS_LITERAL,
                      "var JSCompiler_alias_TRUE=true;"));

    test(generateCode("false", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_FALSE, ENOUGH_TO_ALIAS_LITERAL,
                      "var JSCompiler_alias_FALSE=false;"));

    test(generateCode("null", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_NULL, ENOUGH_TO_ALIAS_LITERAL,
                      "var JSCompiler_alias_NULL=null;"));

    test(generateCode("void 0", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_VOID, ENOUGH_TO_ALIAS_LITERAL,
                     "var JSCompiler_alias_VOID=void 0;"));

    test(generatePreProcessThrowCode(ENOUGH_TO_ALIAS_THROW, "1"),
         generatePostProcessThrowCode(ENOUGH_TO_ALIAS_THROW, "", "1"));
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testAliasTrueFalseNull
  public void testAliasTrueFalseNull() {
    StringBuilder actual = new StringBuilder();
    actual.append(generateCode("true", ENOUGH_TO_ALIAS_LITERAL));
    actual.append(generateCode("false", ENOUGH_TO_ALIAS_LITERAL));
    actual.append(generateCode("null", ENOUGH_TO_ALIAS_LITERAL));
    actual.append(generateCode("void 0", ENOUGH_TO_ALIAS_LITERAL));

    StringBuilder expected = new StringBuilder();
    expected.append(
        "var JSCompiler_alias_VOID=void 0;" +
        "var JSCompiler_alias_TRUE=true;" +
        "var JSCompiler_alias_NULL=null;" +
        "var JSCompiler_alias_FALSE=false;");
    expected.append(
        generateCode(AliasKeywords.ALIAS_TRUE, ENOUGH_TO_ALIAS_LITERAL));
    expected.append(
        generateCode(AliasKeywords.ALIAS_FALSE, ENOUGH_TO_ALIAS_LITERAL));
    expected.append(
        generateCode(AliasKeywords.ALIAS_NULL, ENOUGH_TO_ALIAS_LITERAL));
    expected.append(
        generateCode(AliasKeywords.ALIAS_VOID, ENOUGH_TO_ALIAS_LITERAL));

    test(actual.toString(), expected.toString());
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testAliasThrowKeywordLiteral
  public void testAliasThrowKeywordLiteral() {
    int repitions = Math.max(ENOUGH_TO_ALIAS_THROW, ENOUGH_TO_ALIAS_LITERAL);
    String afterCode = generatePostProcessThrowCode(
          repitions, "var JSCompiler_alias_TRUE=true;",
          AliasKeywords.ALIAS_TRUE);
    test(generatePreProcessThrowCode(repitions, "true"), afterCode);
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testExistingAliasDefinitionFails
  public void testExistingAliasDefinitionFails() {
    try {
      testSame("var JSCompiler_alias_TRUE='foo';");
      fail();
    } catch (RuntimeException expected) {
      
      assertTrue(-1 != expected.getMessage().indexOf(
              "Existing alias definition"));
    }
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testWithNoInputs
  public void testWithNoInputs() {
    testSame(new String[] {});
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering1
  public void testWarningGuardOrdering1() {
    args.add("--jscomp_error=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering2
  public void testWarningGuardOrdering2() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering3
  public void testWarningGuardOrdering3() {
    args.add("--jscomp_warning=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering4
  public void testWarningGuardOrdering4() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_warning=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOffByDefault
  public void testCheckGlobalThisOffByDefault() {
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithAdvancedMode
  public void testCheckGlobalThisOnWithAdvancedMode() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithErrorFlag
  public void testCheckGlobalThisOnWithErrorFlag() {
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOff
  public void testCheckGlobalThisOff() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOffByDefault
  public void testTypeCheckingOffByDefault() {
    test("function f(x) { return x; } f();",
         "function f(a) { return a; } f();");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testReflectedMethods
  public void testReflectedMethods() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test(
        "" +
        "function Foo() {}" +
        "Foo.prototype.handle = function(x, y) { alert(y); };" +
        "var x = goog.reflect.object(Foo, {handle: 1});" +
        "for (var i in x) { x[i].call(x); }" +
        "window['Foo'] = Foo;",
        "function a() {}" +
        "a.prototype.a = function(e, d) { alert(d); };" +
        "var b = goog.c.b(a, {a: 1}),c;" +
        "for (c in b) { b[c].call(b); }" +
        "window.Foo = a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypedAdvanced
  public void testTypedAdvanced() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--use_types_for_optimization");
    test(
        "\n" +
        "function Foo() {}\n" +
        "Foo.prototype.handle1 = function(x, y) { alert(y); };\n" +
        "\n" +
        "function Bar() {}\n" +
        "Bar.prototype.handle1 = function(x, y) {};\n" +
        "new Foo().handle1(1, 2);\n" +
        "new Bar().handle1(1, 2);\n",
        "alert(2)");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOnWithVerbose
  public void testTypeCheckingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test("function f(x) { return x; } f();", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOffByDefault
  public void testTypeParsingOffByDefault() {
    testSame(" function f(a) { return a; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOnWithVerbose
  public void testTypeParsingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride1
  public void testTypeCheckOverride1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=checkTypes");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride2
  public void testTypeCheckOverride2() {
    args.add("--warning_level=DEFAULT");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");

    args.add("--jscomp_warning=checkTypes");
    test("var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOffForDefault
  public void testCheckSymbolsOffForDefault() {
    args.add("--warning_level=DEFAULT");
    test("x = 3; var y; var y;", "x=3; var y;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOnForVerbose
  public void testCheckSymbolsOnForVerbose() {
    args.add("--warning_level=VERBOSE");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
    test("var y; var y;", SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForVerbose
  public void testCheckSymbolsOverrideForVerbose() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=undefinedVars");
    testSame("x = 3;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForQuiet
  public void testCheckSymbolsOverrideForQuiet() {
    args.add("--warning_level=QUIET");
    args.add("--jscomp_error=undefinedVars");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties1
  public void testCheckUndefinedProperties1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_error=missingProperties");
    test("var x = {}; var y = x.bar;", TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties2
  public void testCheckUndefinedProperties2() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=missingProperties");
    test("var x = {}; var y = x.bar;", CheckGlobalNames.UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties3
  public void testCheckUndefinedProperties3() {
    args.add("--warning_level=VERBOSE");
    test("function f() {var x = {}; var y = x.bar;}",
        TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDuplicateParams
  public void testDuplicateParams() {
    test("function f(a, a) {}", RhinoErrorReporter.DUPLICATE_PARAM);
    assertTrue(lastCompiler.hasHaltingErrors());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag
  public void testDefineFlag() {
    args.add("--define=FOO");
    args.add("--define=\"BAR=5\"");
    args.add("--D"); args.add("CCC");
    args.add("-D"); args.add("DDD");
    test(" var FOO = false;" +
         " var BAR = 3;" +
         " var CCC = false;" +
         " var DDD = false;",
         "var FOO = !0, BAR = 5, CCC = !0, DDD = !0;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag2
  public void testDefineFlag2() {
    args.add("--define=FOO='x\"'");
    test(" var FOO = \"a\";",
         "var FOO = \"x\\\"\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag3
  public void testDefineFlag3() {
    args.add("--define=FOO=\"x'\"");
    test(" var FOO = \"a\";",
         "var FOO = \"x'\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testScriptStrictModeNoWarning
  public void testScriptStrictModeNoWarning() {
    test("'use strict';", "");
    test("'no use strict';", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testFunctionStrictModeNoWarning
  public void testFunctionStrictModeNoWarning() {
    test("function f() {'use strict';}", "function f() {}");
    test("function f() {'no use strict';}",
         CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testQuietMode
  public void testQuietMode() {
    args.add("--warning_level=DEFAULT");
    test(" var x;",
         RhinoErrorReporter.PARSE_ERROR);
    args.add("--warning_level=QUIET");
    testSame(" var x;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessClosurePrimitives
  public void testProcessClosurePrimitives() {
    test("var goog = {}; goog.provide('goog.dom');",
         "var goog = {dom:{}};");
    args.add("--process_closure_primitives=false");
    testSame("var goog = {}; goog.provide('goog.dom');");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGetMsgWiring
  public void testGetMsgWiring() throws Exception {
    test("var goog = {}; goog.getMsg = function(x) { return x; };" +
         " var MSG_FOO = goog.getMsg('foo');",
         "var goog={getMsg:function(a){return a}}, " +
         "MSG_FOO=goog.getMsg('foo');");
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("var goog = {}; goog.getMsg = function(x) { return x; };" +
         " var MSG_FOO = goog.getMsg('foo');" +
         "window['foo'] = MSG_FOO;",
         "window.foo = 'foo';");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCssNameWiring
  public void testCssNameWiring() throws Exception {
    test("var goog = {}; goog.getCssName = function() {};" +
         "goog.setCssNameMapping = function() {};" +
         "goog.setCssNameMapping({'goog': 'a', 'button': 'b'});" +
         "var a = goog.getCssName('goog-button');" +
         "var b = goog.getCssName('css-button');" +
         "var c = goog.getCssName('goog-menu');" +
         "var d = goog.getCssName('css-menu');",
         "var goog = { getCssName: function() {}," +
         "             setCssNameMapping: function() {} }," +
         "    a = 'a-b'," +
         "    b = 'css-b'," +
         "    c = 'a-menu'," +
         "    d = 'css-menu';");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue70a
  public void testIssue70a() {
    test("function foo({}) {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue70b
  public void testIssue70b() {
    test("function foo([]) {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue81
  public void testIssue81() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    useStringComparison = true;
    test("eval('1'); var x = eval; x('2');",
         "eval(\"1\");(0,eval)(\"2\");");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue115
  public void testIssue115() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--jscomp_off=es5Strict");
    args.add("--warning_level=VERBOSE");
    test("function f() { " +
         "  var arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}",
         "function f() { " +
         "  arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue297
  public void testIssue297() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function f(p) {" +
         " var x;" +
         " return ((x=p.id) && (x=parseInt(x.substr(1))) && x>0);" +
         "}",
         "function f(b) {" +
         " var a;" +
         " return ((a=b.id) && (a=parseInt(a.substr(1))) && 0<a);" +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testHiddenSideEffect
  public void testHiddenSideEffect() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("element.offsetWidth;",
         "element.offsetWidth", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue504
  public void testIssue504() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("void function() { alert('hi'); }();",
         "alert('hi');void 0", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601
  public void testIssue601() {
    args.add("--compilation_level=WHITESPACE_ONLY");
    test("function f() { return '\\v' == 'v'; } window['f'] = f;",
         "function f(){return'\\v'=='v'}window['f']=f");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601b
  public void testIssue601b() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { return '\\v' == 'v'; } window['f'] = f;",
         "window.f=function(){return'\\v'=='v'}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601c
  public void testIssue601c() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { return '\\u000B' == 'v'; } window['f'] = f;",
         "window.f=function(){return'\\u000B'=='v'}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag1
  public void testDebugFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=false");
    test("function foo(a) {}",
         "function foo() {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag2
  public void testDebugFlag2() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=true");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag3
  public void testDebugFlag3() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=false");
    test("function Foo() {}" +
         "Foo.x = 1;" +
         "function f() {throw new Foo().x;} f();",
         "throw (new function() {}).a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag4
  public void testDebugFlag4() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=true");
    test("function Foo() {}" +
        "Foo.x = 1;" +
        "function f() {throw new Foo().x;} f();",
        "throw (new function Foo() {}).$x$;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag1
  public void testBooleanFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag2
  public void testBooleanFlag2() {
    args.add("--debug");
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testHelpFlag
  public void testHelpFlag() {
    args.add("--help");
    assertFalse(
        createCommandLineRunner(
            new String[] {"function f() {}"}).shouldRunCompiler());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting1
  public void testExternsLifting1() throws Exception{
    String code = " function f() {}";
    test(new String[] {code},
         new String[] {});

    assertEquals(2, lastCompiler.getExternsForTesting().size());

    CompilerInput extern = lastCompiler.getExternsForTesting().get(1);
    assertNull(extern.getModule());
    assertTrue(extern.isExtern());
    assertEquals(code, extern.getCode());

    assertEquals(1, lastCompiler.getInputsForTesting().size());

    CompilerInput input = lastCompiler.getInputsForTesting().get(0);
    assertNotNull(input.getModule());
    assertFalse(input.isExtern());
    assertEquals("", input.getCode());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting2
  public void testExternsLifting2() {
    args.add("--warning_level=VERBOSE");
    test(new String[] {" function f() {}", "f(3);"},
         new String[] {"f(3);"},
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOff
  public void testSourceSortingOff() {
    args.add("--compilation_level=WHITESPACE_ONLY");
    testSame(
        new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
        });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn
  public void testSourceSortingOn() {
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn2
  public void testSourceSortingOn2() {
    test(new String[] {
          "goog.provide('a');",
          "goog.require('a');\n" +
          "var COMPILED = false;",
         },
         new String[] {
           "var a={};",
           "var COMPILED=!1"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn3
  public void testSourceSortingOn3() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.addDependency('sym', [], []);\nvar x = 3;",
          "var COMPILED = false;",
         },
         new String[] {
          "var COMPILED = !1;",
          "var x = 3;"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps1
  public void testSourceSortingCircularDeps1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps2
  public void testSourceSortingCircularDeps2() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('roses.lime.juice');",
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');",
          "goog.provide('gimlet');" +
          "     goog.require('gin'); goog.require('roses.lime.juice');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn1
  public void testSourcePruningOn1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn2
  public void testSourcePruningOn2() {
    args.add("--closure_entry_point=guinness");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var guinness = {};"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn3
  public void testSourcePruningOn3() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn4
  public void testSourcePruningOn4() {
    args.add("--closure_entry_point=scotch");
    args.add("--closure_entry_point=beer");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn5
  public void testSourcePruningOn5() {
    args.add("--closure_entry_point=shiraz");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         Compiler.MISSING_ENTRY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn6
  public void testSourcePruningOn6() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn7
  public void testSourcePruningOn7() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "var COMPILED = false;",
         },
         new String[] {
          "var COMPILED = !1;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn8
  public void testSourcePruningOn8() {
    args.add("--only_closure_dependencies");
    args.add("--closure_entry_point=scotch");
    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "\n" +
          "var externVar;",
          "goog.provide('scotch'); var x = externVar;"
         },
         new String[] {
           "var scotch = {}, x = externVar;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testNoCompile
  public void testNoCompile() {
    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "\n" +
          "goog.provide('x');\n" +
          "var dupeVar;",
          "var dupeVar;"
         },
         new String[] {
           "var dupeVar;"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDependencySortingWhitespaceMode
  public void testDependencySortingWhitespaceMode() {
    args.add("--manage_closure_dependencies");
    args.add("--compilation_level=WHITESPACE_ONLY");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');\ngoog.require('hops');",
          "goog.provide('hops');",
         },
         new String[] {
          "goog.provide('hops');",
          "goog.provide('beer');\ngoog.require('hops');",
          "goog.require('beer');"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testForwardDeclareDroppedTypes
  public void testForwardDeclareDroppedTypes() {
    args.add("--manage_closure_dependencies=true");

    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}",
          "goog.provide('Scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         });

    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         },
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testOnlyClosureDependenciesEmptyEntryPoints
  public void testOnlyClosureDependenciesEmptyEntryPoints() throws Exception {
    
    args.add("--use_only_custom_externs=true");

    args.add("--only_closure_dependencies=true");
    try {
      CommandLineRunner runner = createCommandLineRunner(new String[0]);
      runner.doRun();
      fail("Expected FlagUsageException");
    } catch (FlagUsageException e) {
      assertTrue(e.getMessage(),
          e.getMessage().contains("only_closure_dependencies"));
    }
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testOnlyClosureDependenciesOneEntryPoint
  public void testOnlyClosureDependenciesOneEntryPoint() throws Exception {
    args.add("--only_closure_dependencies=true");
    args.add("--closure_entry_point=beer");
    test(new String[] {
          "goog.require('beer'); var beerRequired = 1;",
          "goog.provide('beer');\ngoog.require('hops');\nvar beerProvided = 1;",
          "goog.provide('hops'); var hopsProvided = 1;",
          "goog.provide('scotch'); var scotchProvided = 1;",
          "goog.require('scotch');\nvar includeFileWithoutProvides = 1;",
          "\nvar COMPILED = false;",
         },
         new String[] {
           "var COMPILED = !1;",
           "var hops = {}, hopsProvided = 1;",
           "var beer = {}, beerProvided = 1;"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion1
  public void testSourceMapExpansion1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    args.add("--create_source_map=%outname%.map");
    testSame("var x = 3;");
    assertEquals("/path/to/out.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion2
  public void testSourceMapExpansion2() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion3
  public void testSourceMapExpansion3() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo_");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo_m0.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(),
            lastCompiler.getModuleGraph().getRootModule()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat1
  public void testSourceMapFormat1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.DEFAULT,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat2
  public void testSourceMapFormat2() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    args.add("--source_map_format=V3");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.V3,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testModuleWrapperBaseNameExpansion
  public void testModuleWrapperBaseNameExpansion() throws Exception {
    useModules = ModulePattern.CHAIN;
    args.add("--module_wrapper=m0:%s 
    testSame(new String[] {
      "var x = 3;",
      "var y = 4;"
    });

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.writeModuleOutput(
        builder,
        lastCompiler.getModuleGraph().getRootModule());
    assertEquals("var x=3; 
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCharSetExpansion
  public void testCharSetExpansion() {
    testSame("");
    assertEquals("US-ASCII", lastCompiler.getOptions().outputCharset);
    args.add("--charset=UTF-8");
    testSame("");
    assertEquals("UTF-8", lastCompiler.getOptions().outputCharset);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testChainModuleManifest
  public void testChainModuleManifest() throws Exception {
    useModules = ModulePattern.CHAIN;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestOrBundleTo(
        lastCompiler.getModuleGraph(), builder, true);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m1}\n" +
        "i2\n" +
        "\n" +
        "{m3:m2}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testStarModuleManifest
  public void testStarModuleManifest() throws Exception {
    useModules = ModulePattern.STAR;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestOrBundleTo(
        lastCompiler.getModuleGraph(), builder, true);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m0}\n" +
        "i2\n" +
        "\n" +
        "{m3:m0}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag
  public void testVersionFlag() {
    args.add("--version");
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag2
  public void testVersionFlag2() {
    lastArg = "--version";
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testPrintAstFlag
  public void testPrintAstFlag() {
    args.add("--print_ast=true");
    testSame("");
    assertEquals(
        "digraph AST {\n" +
        "  node [color=lightblue2, style=filled];\n" +
        "  node0 [label=\"BLOCK\"];\n" +
        "  node1 [label=\"SCRIPT\"];\n" +
        "  node0 -> node1 [weight=1];\n" +
        "  node1 -> RETURN [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> RETURN [label=\"SYN_BLOCK\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> node1 [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "}\n\n",
        new String(outReader.toByteArray()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSyntheticExterns
  public void testSyntheticExterns() {
    externs = ImmutableList.of(
        SourceFile.fromCode("externs", "myVar.property;"));
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR);

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         "var theirVar={},myVar={},yourVar={};");

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var myVar = {};",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGoogAssertStripping
  public void testGoogAssertStripping() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("goog.asserts.assert(false)",
         "");
    args.add("--debug");
    test("goog.asserts.assert(false)", "goog.$asserts$.$assert$(!1)");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testMissingReturnCheckOnWithVerbose
  public void testMissingReturnCheckOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {f()} f();",
        CheckMissingReturn.MISSING_RETURN_STATEMENT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGenerateExports
  public void testGenerateExports() {
    args.add("--generate_exports=true");
    test(" foo.prototype.x = function() {};",
        "foo.prototype.x=function(){};"+
        "goog.exportSymbol(\"foo.prototype.x\",foo.prototype.x);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDepreciationWithVerbose
  public void testDepreciationWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {}; f()",
       CheckAccessControls.DEPRECATED_NAME);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTwoParseErrors
  public void testTwoParseErrors() {
    
    
    Compiler compiler = compile(new String[] {
      "var a b;",
      "var b c;"
    });
    assertEquals(2, compiler.getErrors().length);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES3ByDefault
  public void testES3ByDefault() {
    test("var x = f.function", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5
  public void testES5() {
    args.add("--language_in=ECMASCRIPT5");
    test("var x = f.function", "var x = f.function");
    test("var let", "var let");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5Strict
  public void testES5Strict() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    test("var x = f.function", "'use strict';var x = f.function");
    test("var let", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5StrictUseStrict
  public void testES5StrictUseStrict() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    Compiler compiler = compile(new String[] {"var x = f.function"});
    String outputSource = compiler.toSource();
    assertEquals("'use strict'", outputSource.substring(0, 12));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5StrictUseStrictMultipleInputs
  public void testES5StrictUseStrictMultipleInputs() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    Compiler compiler = compile(new String[] {"var x = f.function",
        "var y = f.function", "var z = f.function"});
    String outputSource = compiler.toSource();
    assertEquals("'use strict'", outputSource.substring(0, 12));
    assertEquals(outputSource.substring(13).indexOf("'use strict'"), -1);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWithKeywordDefault
  public void testWithKeywordDefault() {
    test("var x = {}; with (x) {}", ControlStructureCheck.USE_OF_WITH);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWithKeywordWithEs5ChecksOff
  public void testWithKeywordWithEs5ChecksOff() {
    args.add("--jscomp_off=es5Strict");
    testSame("var x = {}; with (x) {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testNoSrCFilesWithManifest
  public void testNoSrCFilesWithManifest() throws IOException {
    args.add("--use_only_custom_externs=true");
    args.add("--output_manifest=test.MF");
    CommandLineRunner runner = createCommandLineRunner(new String[0]);
    String expectedMessage = "";
    try {
      runner.doRun();
    } catch (FlagUsageException e) {
      expectedMessage = e.getMessage();
    }
    assertEquals(expectedMessage, "Bad --js flag. " +
      "Manifest files cannot be generated when the input is from stdin.");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTransformAMD
  public void testTransformAMD() {
    args.add("--transform_amd_modules");
    test("define({test: 1})", "exports = {test: 1}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessCJS
  public void testProcessCJS() {
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    setFilename(0, "foo/bar.js");
    test("exports.test = 1",
        "var module$foo$bar={test:1}; " +
        "module$foo$bar.module$exports && " +
        "(module$foo$bar=module$foo$bar.module$exports)");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTransformAMDAndProcessCJS
  public void testTransformAMDAndProcessCJS() {
    args.add("--transform_amd_modules");
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    setFilename(0, "foo/bar.js");
    test("define({foo: 1})",
        "var module$foo$bar={}, module$foo$bar={foo:1}; " +
        "module$foo$bar.module$exports && " +
        "(module$foo$bar=module$foo$bar.module$exports)");
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderColumnAfterResetDummy
  public void testCodeBuilderColumnAfterResetDummy() {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("");
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderColumnAfterReset
  public void testCodeBuilderColumnAfterReset() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    String js = "foo();\ngoo();";
    cb.append(js);
    assertEquals(js, cb.toString());
    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());

    cb.reset();

    assertTrue(cb.toString().isEmpty());
    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderAppend
  public void testCodeBuilderAppend() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("foo();");
    assertEquals(0, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());

    cb.append("goo();");

    assertEquals(0, cb.getLineIndex());
    assertEquals(12, cb.getColumnIndex());

    
    cb.append("blah();\ngoo();");

    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());
  }

// com.google.javascript.jscomp.CompilerTest::testCyclicalDependencyInInputs
  public void testCyclicalDependencyInInputs() {
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode(
            "gin", "goog.provide('gin'); goog.require('tonic'); var gin = {};"),
        SourceFile.fromCode("tonic",
            "goog.provide('tonic'); goog.require('gin'); var tonic = {};"),
        SourceFile.fromCode(
            "mix", "goog.require('gin'); goog.require('tonic');"));
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;
    options.setManageClosureDependencies(true);
    Compiler compiler = new Compiler();
    compiler.init(ImmutableList.<SourceFile>of(), inputs, options);
    compiler.parseInputs();
    assertEquals(compiler.externAndJsRoot, compiler.jsRoot.getParent());
    assertEquals(compiler.externAndJsRoot, compiler.externsRoot.getParent());
    assertNotNull(compiler.externAndJsRoot);

    Node jsRoot = compiler.jsRoot;
    assertEquals(3, jsRoot.getChildCount());
  }

// com.google.javascript.jscomp.CompilerTest::testLocalUndefined
  public void testLocalUndefined() throws Exception {
    
    
    
    
    
    
    
    
    CompilerOptions options = new CompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(
        options);
    Compiler compiler = new Compiler();
    SourceFile externs = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js",
        "(function (undefined) { alert(undefined); })();");
    compiler.compile(externs, input, options);
  }

// com.google.javascript.jscomp.CompilerTest::testCommonJSProvidesAndRequire
  public void testCommonJSProvidesAndRequire() throws Exception {
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode("gin.js", "require('tonic')"),
        SourceFile.fromCode("tonic.js", ""),
        SourceFile.fromCode("mix.js", "require('gin'); require('tonic');"));
    List<String> entryPoints = Lists.newArrayList("module$mix");

    Compiler compiler = initCompilerForCommonJS(inputs, entryPoints);
    JSModuleGraph graph = compiler.getModuleGraph();
    assertEquals(graph.getModuleCount(), 3);
    List<CompilerInput> result = graph.manageDependencies(entryPoints,
        compiler.getInputsForTesting());
    assertEquals("[module$tonic]", result.get(0).getName());
    assertEquals("[module$gin]", result.get(1).getName());
    assertEquals("tonic.js", result.get(2).getName());
    assertEquals("gin.js", result.get(3).getName());
    assertEquals("mix.js", result.get(4).getName());
  }

// com.google.javascript.jscomp.CompilerTest::testCommonJSMissingRequire
  public void testCommonJSMissingRequire() throws Exception {
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode("gin.js", "require('missing')"));
    Compiler compiler = initCompilerForCommonJS(
        inputs, ImmutableList.of("module$gin"));
    compiler.processAMDAndCommonJSModules();

    assertEquals(1, compiler.getErrorManager().getErrorCount());
    String error = compiler.getErrorManager().getErrors()[0].toString();
    assertTrue(
        "Unexpected error: " + error,
        error.contains(
            "required entry point \"module$missing\" never provided"));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction1
  public void testIsSimpleFunction1() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction2
  public void testIsSimpleFunction2() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return 0;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction3
  public void testIsSimpleFunction3() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return x ? 0 : 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction4
  public void testIsSimpleFunction4() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction5
  public void testIsSimpleFunction5() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return 0; return 0;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction6
  public void testIsSimpleFunction6() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){var x=true;return x ? 0 : 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction7
  public void testIsSimpleFunction7() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){if (x) return 0; else return 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction1
  public void testCanInlineReferenceToFunction1() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction2
  public void testCanInlineReferenceToFunction2() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction3
  public void testCanInlineReferenceToFunction3() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return;}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction4
  public void testCanInlineReferenceToFunction4() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return;}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction5
  public void testCanInlineReferenceToFunction5() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction6
  public void testCanInlineReferenceToFunction6() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction7
  public void testCanInlineReferenceToFunction7() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x=foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction8
  public void testCanInlineReferenceToFunction8() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x=foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction9
  public void testCanInlineReferenceToFunction9() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction10
  public void testCanInlineReferenceToFunction10() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction11
  public void testCanInlineReferenceToFunction11() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=x+foo();", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction12
  public void testCanInlineReferenceToFunction12() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; var x; x=x+foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction12b
  public void testCanInlineReferenceToFunction12b() {
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return true;}; var x; x=x+foo();",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction14
  public void testCanInlineReferenceToFunction14() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; foo(x);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction15
  public void testCanInlineReferenceToFunction15() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; foo(x);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction16
  public void testCanInlineReferenceToFunction16() {
    
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){var b;return a;}; foo(goo());", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction17
  public void testCanInlineReferenceToFunction17() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction18
  public void testCanInlineReferenceToFunction18() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a;} foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction19
  public void testCanInlineReferenceToFunction19() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo([]);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction20
  public void testCanInlineReferenceToFunction20() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo({});", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction21
  public void testCanInlineReferenceToFunction21() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(new Date);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction22
  public void testCanInlineReferenceToFunction22() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(true && new Date);", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction23
  public void testCanInlineReferenceToFunction23() {
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction24
  public void testCanInlineReferenceToFunction24() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction25
  public void testCanInlineReferenceToFunction25() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction26
  public void testCanInlineReferenceToFunction26() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction27
  public void testCanInlineReferenceToFunction27() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a+a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction28
  public void testCanInlineReferenceToFunction28() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; foo(goo());", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction29
  public void testCanInlineReferenceToFunction29() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return true;}; foo(goo());", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction30
  public void testCanInlineReferenceToFunction30() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo(goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction31
  public void testCanInlineReferenceToFunction31() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a) {return true;}; " +
        "function x() {foo.call(this, 1);}",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction32
  public void testCanInlineReferenceToFunction32() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.apply(this, [1]); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction33
  public void testCanInlineReferenceToFunction33() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.bar(this, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction34
  public void testCanInlineReferenceToFunction34() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction35
  public void testCanInlineReferenceToFunction35() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.apply(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction36
  public void testCanInlineReferenceToFunction36() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.bar(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction37
  public void testCanInlineReferenceToFunction37() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction38
  public void testCanInlineReferenceToFunction38() {
    assumeStrictThis = false;

    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, goo()); }",
        "foo", INLINE_BLOCK);

    assumeStrictThis = true;

    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction39
  public void testCanInlineReferenceToFunction39() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction40
  public void testCanInlineReferenceToFunction40() {
    assumeStrictThis = false;
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, goo()); }",
        "foo", INLINE_BLOCK);

    assumeStrictThis = true;
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction41
  public void testCanInlineReferenceToFunction41() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction42
  public void testCanInlineReferenceToFunction42() {
    assumeStrictThis = false;
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), goo()); }",
        "foo", INLINE_BLOCK);

    assumeStrictThis = true;
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction43
  public void testCanInlineReferenceToFunction43() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction44
  public void testCanInlineReferenceToFunction44() {
    assumeStrictThis = false;
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_BLOCK);

    assumeStrictThis = true;
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction45
  public void testCanInlineReferenceToFunction45() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction46
  public void testCanInlineReferenceToFunction46() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction47
  public void testCanInlineReferenceToFunction47() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){var a; return function() {return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction48
  public void testCanInlineReferenceToFunction48() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){var a; return function() {return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction49
  public void testCanInlineReferenceToFunction49() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {var a; return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction50
  public void testCanInlineReferenceToFunction50() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {var a; return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction51
  public void testCanInlineReferenceToFunction51() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){function x() {var a; return true;} return x}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression1
  public void testCanInlineReferenceToFunctionInExpression1() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression2
  public void testCanInlineReferenceToFunctionInExpression2() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression3
  public void testCanInlineReferenceToFunctionInExpression3() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression4
  public void testCanInlineReferenceToFunctionInExpression4() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression5
  public void testCanInlineReferenceToFunctionInExpression5() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {true?foo(1):1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression5a
 public void testCanInlineReferenceToFunctionInExpression5a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {true?foo(1):1 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression6
  public void testCanInlineReferenceToFunctionInExpression6() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression7
  public void testCanInlineReferenceToFunctionInExpression7() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {1 && foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression7a
  public void testCanInlineReferenceToFunctionInExpression7a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {1 && foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression8
  public void testCanInlineReferenceToFunctionInExpression8() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression9
  public void testCanInlineReferenceToFunctionInExpression9() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression10
  public void testCanInlineReferenceToFunctionInExpression10() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {var b; b += 1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression10a
  public void testCanInlineReferenceToFunctionInExpression10a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {var b; b += 1 + foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression12
  public void testCanInlineReferenceToFunctionInExpression12() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var a,b,c; a = b = c = foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression13
  public void testCanInlineReferenceToFunctionInExpression13() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var a,b,c; a = b = c = 1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression14
  public void testCanInlineReferenceToFunctionInExpression14() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "var a = {}, b = {}, c;" +
        "a.test = 'a';" +
        "b.test = 'b';" +
        "c = a;" +
        "function foo(){c = b; return 'foo'};" +
        "c.test=foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression14a
  public void testCanInlineReferenceToFunctionInExpression14a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "var a = {}, b = {}, c;" +
        "a.test = 'a';" +
        "b.test = 'b';" +
        "c = a;" +
        "function foo(){c = b; return 'foo'};" +
        "c.test=foo();",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression18
  public void testCanInlineReferenceToFunctionInExpression18() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return _g();}; " +
        "function x() {1 + foo()() }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression19
  public void testCanInlineReferenceToFunctionInExpression19() {
    
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(foo()) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression19a
  public void testCanInlineReferenceToFunctionInExpression19a() {
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(foo()) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression21
  public void testCanInlineReferenceToFunctionInExpression21() {
    
    
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression21a
  public void testCanInlineReferenceToFunctionInExpression21a() {
    
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression22
  public void testCanInlineReferenceToFunctionInExpression22() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo()) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression22a
  public void testCanInlineReferenceToFunctionInExpression22a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo()) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression23
  public void testCanInlineReferenceToFunctionInExpression23() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo.call(this)) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression23a
  public void testCanInlineReferenceToFunctionInExpression23a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo.call(this)) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInLoop1
  public void testCanInlineReferenceToFunctionInLoop1() {
    helperCanInlineReferenceToFunction(
        CanInlineResult.YES,
        "function foo(){return a;}; " +
        "while(1) { foo(); }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInLoop2
  public void testCanInlineReferenceToFunctionInLoop2() {
    
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.NO,
        "function foo(){return function() {};}; " +
        "while(1) { foo(); }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline1
  public void testInline1() {
    helperInlineReferenceToFunction(
        "function foo(){}; foo();",
        "function foo(){}; void 0",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline2
  public void testInline2() {
    helperInlineReferenceToFunction(
        "function foo(){}; foo();",
        "function foo(){}; {}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline3
  public void testInline3() {
    helperInlineReferenceToFunction(
        "function foo(){return;}; foo();",
        "function foo(){return;}; {}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline4
  public void testInline4() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; foo();",
        "function foo(){return true;}; true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline5
  public void testInline5() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; foo();",
        "function foo(){return true;}; {true;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline6
  public void testInline6() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x=foo();",
        "function foo(){return true;}; var x=true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline7
  public void testInline7() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x=foo();",
        "function foo(){return true;}; var x;" +
            "{x=true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline8
  public void testInline8() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=foo();",
        "function foo(){return true;}; var x; x=true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline9
  public void testInline9() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=foo();",
        "function foo(){return true;}; var x;{x=true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline10
  public void testInline10() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=x+foo();",
        "function foo(){return true;}; var x; x=x+true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline11
  public void testInline11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(x);",
        "function foo(a){return true;}; true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline12
  public void testInline12() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(x);",
        "function foo(a){return true;}; {true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline13
  public void testInline13() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "function foo(a){return a;}; " +
        "function x() {{var a$$inline_0=x++;" +
            "a$$inline_0}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline14
  public void testInline14() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(x++);",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_0=x++;" +
            " a$$inline_0+" +
            "a$$inline_0;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline15
  public void testInline15() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(new Date());",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_0=new Date();" +
            " a$$inline_0+" +
            "a$$inline_0;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline16
  public void testInline16() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(function(){});",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_0=function(){};" +
            " a$$inline_0+" +
            "a$$inline_0;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline17
  public void testInline17() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(goo());",
        "function foo(a){return true;};" +
            "{var a$$inline_0=goo();true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline18
  public void testInline18() {
    
    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
            "function x() { foo(goo()); }",
            "function foo(a){var b;return a;}; " +
            "function x() {{var a$$inline_0=goo();" +
                "var b$$inline_1;a$$inline_0}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline19
  public void testInline19() {
    
    helperInlineReferenceToFunction(
        "var x = 1; var y = 2;" +
        "function foo(a,b){x = b; y = a;}; " +
        "function bar() { foo(x,y); }",
        "var x = 1; var y = 2;" +
        "function foo(a,b){x = b; y = a;}; " +
        "function bar() {" +
           "{var a$$inline_0=x;" +
            "x = y;" +
            "y = a$$inline_0;}" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline19b
  public void testInline19b() {
    helperInlineReferenceToFunction(
        "var x = 1; var y = 2;" +
        "function foo(a,b){y = a; x = b;}; " +
        "function bar() { foo(x,y); }",
        "var x = 1; var y = 2;" +
        "function foo(a,b){y = a; x = b;}; " +
        "function bar() {" +
           "{var b$$inline_1=y;" +
            "y = x;" +
            "x = b$$inline_1;}" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineIntoLoop
  public void testInlineIntoLoop() {
    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
        "for(;1;){ foo(1); }",
        "function foo(a){var b;return a;}; " +
        "for(;1;){ {" +
            "var b$$inline_1=void 0;1}}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
        "do{ foo(1); } while(1)",
        "function foo(a){var b;return a;}; " +
        "do{ {" +
            "var b$$inline_1=void 0;1}}while(1)",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){for(var b in c)return a;}; " +
        "for(;1;){ foo(1); }",
        "function foo(a){var b;for(b in c)return a;}; " +
        "for(;1;){ {JSCompiler_inline_label_foo_2:{" +
            "var b$$inline_1=void 0;for(b$$inline_1 in c){" +
              "1;break JSCompiler_inline_label_foo_2" +
            "}}}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction1
  public void testInlineFunctionWithInnerFunction1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {return true;}}; foo();",
        "function foo(){return function() {return true;}};" +
            "(function() {return true;})",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction2
  public void testInlineFunctionWithInnerFunction2() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {return true;}}; foo();",
        "function foo(){return function() {return true;}};" +
            "{(function() {return true;})}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction3
  public void testInlineFunctionWithInnerFunction3() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {var a; return true;}}; foo();",
        "function foo(){return function() {var a; return true;}};" +
            "(function() {var a; return true;});",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction4
  public void testInlineFunctionWithInnerFunction4() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {var a; return true;}}; foo();",
        "function foo(){return function() {var a; return true;}};" +
            "{(function() {var a$$inline_0; return true;});}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction5
  public void testInlineFunctionWithInnerFunction5() {
    
    helperInlineReferenceToFunction(
        "function foo(){function x() {var a; return true;} return x}; foo();",
        "function foo(){function x(){var a;return true}return x};" +
            "{var x$$inline_0 = function(){" +
            "var a$$inline_1;return true};x$$inline_0}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression1
  public void testInlineReferenceInExpression1() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "if (JSCompiler_inline_result$$0) throw 'test'; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression2
  public void testInlineReferenceInExpression2() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "return JSCompiler_inline_result$$0; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression3
  public void testInlineReferenceInExpression3() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "switch(JSCompiler_inline_result$$0) { default:break; } }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression4
  public void testInlineReferenceInExpression4() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0?0:1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression5
  public void testInlineReferenceInExpression5() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)&&1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0&&1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression6
  public void testInlineReferenceInExpression6() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression7
  public void testInlineReferenceInExpression7() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0&&1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression8
  public void testInlineReferenceInExpression8() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression9
  public void testInlineReferenceInExpression9() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "var b = 1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression11
  public void testInlineReferenceInExpression11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {a:foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { a:{{var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0?0:1 }}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression12
  public void testInlineReferenceInExpression12() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1?foo(1):1 }",
        "function foo(a){return true;}; " +
        "function x() { if(1) { {true;} } else { 1 }}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression13
  public void testInlineReferenceInExpression13() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { goo() + (1?foo(1):1) }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_temp_const$$0=goo();" +
            "var JSCompiler_temp$$1;" +
            "if(1) {" +
            "  {JSCompiler_temp$$1=true;} " +
            "} else {" +
            "  JSCompiler_temp$$1=1;" +
            "}" +
            "JSCompiler_temp_const$$0 + JSCompiler_temp$$1" +
            "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression14
  public void testInlineReferenceInExpression14() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression15
  public void testInlineReferenceInExpression15() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo.call(this,1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression16
  public void testInlineReferenceInExpression16() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z[bar()] = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$1=z;" +
            "var JSCompiler_temp_const$$0=bar();" +
            "{" +
             "var JSCompiler_inline_result$$2;" +
             "z= {};" +
             "JSCompiler_inline_result$$2 = true;" +
            "}" +
            "JSCompiler_temp_const$$1[JSCompiler_temp_const$$0] = " +
                "JSCompiler_inline_result$$2;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression17
  public void testInlineReferenceInExpression17() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.y.x.gack = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z.y.x;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineWithinCalls1
  public void testInlineWithinCalls1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x() {1 + foo()() }",
        "function foo(){return _g;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=_g;}" +
        "1 + JSCompiler_inline_result$$0() }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineAssignmentToConstant
  public void testInlineAssignmentToConstant() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x(){var CONSTANT_RESULT = foo(); }",

        "function foo(){return _g;}; " +
        "function x() {" +
        "  {var JSCompiler_inline_result$$0; JSCompiler_inline_result$$0=_g;}" +
        "  var CONSTANT_RESULT = JSCompiler_inline_result$$0;" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testBug1897706
  public void testBug1897706() {
    helperInlineReferenceToFunction(
        "function foo(a){}; foo(x())",
        "function foo(a){}; {var a$$inline_0=x()}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){bar()}; foo(x())",
        "function foo(a){bar()}; {var a$$inline_0=x();bar()}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a,b){bar()}; foo(x(),y())",
        "function foo(a,b){bar()};" +
        "{var a$$inline_0=x();var b$$inline_1=y();bar()}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst1
  public void testReplaceReturnConst1() {
    String source = "a.prototype.foo = function() {return \"foobar\"}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(\"foobar\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst2
  public void testReplaceReturnConst2() {
    checkCompilesToSame("a.prototype.foo = function() {return foobar}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst3
  public void testReplaceReturnConst3() {
    String source = "a.prototype.foo = function() {return void 0;}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(void 0)",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter1
  public void testReplaceGetter1() {
    String source = "a.prototype.foo = function() {return this.foo_}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    GET_HELPER,
                    "a.prototype.foo = JSCompiler_get(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter2
  public void testReplaceGetter2() {
    checkCompilesToSame("a.prototype.foo = function() {return}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter1
  public void testReplaceSetter1() {
    String source = "a.prototype.foo = function(v) {this.foo_ = v}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter2
  public void testReplaceSetter2() {
    String source = "a.prototype.foo = function(v, v2) {this.foo_ = v}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter3
  public void testReplaceSetter3() {
    checkCompilesToSame("a.prototype.foo = function() {this.foo_ = v}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter4
  public void testReplaceSetter4() {
    checkCompilesToSame(
        "a.prototype.foo = function(v, v2) {this.foo_ = v2}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction1
  public void testReplaceEmptyFunction1() {
    String source = "a.prototype.foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "a.prototype.foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction2
  public void testReplaceEmptyFunction2() {
    checkCompilesToSame("function foo() {}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction3
  public void testReplaceEmptyFunction3() {
    String source = "var foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "var foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction1
  public void testReplaceIdentityFunction1() {
    String source = "a.prototype.foo = function(a) {return a}";
    checkCompilesToSame(source, 2);
    checkCompilesTo(source,
                    IDENTITY_HELPER,
                    "a.prototype.foo = JSCompiler_identityFn()",
                    3);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction2
  public void testReplaceIdentityFunction2() {
    checkCompilesToSame("a.prototype.foo = function(a) {return a + 1}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testIssue538
  public void testIssue538() {
    checkCompilesToSame(      "\n" +
        "WebInspector.Setting = function() {}\n" +
        "WebInspector.Setting.prototype = {\n" +
        "    get name0(){return this._name;},\n" +
        "    get name1(){return this._name;},\n" +
        "    get name2(){return this._name;},\n" +
        "    get name3(){return this._name;},\n" +
        "    get name4(){return this._name;},\n" +
        "    get name5(){return this._name;},\n" +
        "    get name6(){return this._name;},\n" +
        "    get name7(){return this._name;},\n" +
        "    get name8(){return this._name;},\n" +
        "    get name9(){return this._name;},\n" +
        "}", 1);
  }

// com.google.javascript.jscomp.InlineCostEstimatorTest::testCost
  public void testCost() {
    checkCost("1", "1");
    checkCost("true", "1");
    checkCost("false", "1");
    checkCost("a", "xx");
    checkCost("a + b", "xx+xx");
    checkCost("foo()", "xx()");
    checkCost("foo(a,b)", "xx(xx,xx)");
    checkCost("10 + foo(a,b)", "0+xx(xx,xx)");
    checkCost("1 + foo(a,b)", "1+xx(xx,xx)");
    checkCost("a ? 1 : 0", "xx?1:0");
    checkCost("a.b", "xx.xx");
    checkCost("new Obj()", "new xx");
    checkCost("function a() {return \"monkey\"}",
              "function xx(){return\"monkey\"}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction1
  public void testInlineEmptyFunction1() {
    
    test("function foo(){}" +
        "foo();",
        "void 0;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction2
  public void testInlineEmptyFunction2() {
    
    test("function foo(){}" +
        "foo(1, new Date, function(){});",
        "void 0;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction3
  public void testInlineEmptyFunction3() {
    
    test("function foo(){}" +
        "foo();foo();foo();",
        "void 0;void 0;void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction4
  public void testInlineEmptyFunction4() {
    
    test("function foo(){}" +
        "foo(x());",
        "{var JSCompiler_inline_anon_param_0=x();}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction5
  public void testInlineEmptyFunction5() {
    
    
    allowBlockInlining = false;
    testSame("function foo(){}" +
        "foo(x());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions1
  public void testInlineFunctions1() {
    
    test("function foo(){ return 4 }" +
        "foo();",
        "4");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions2
  public void testInlineFunctions2() {
    
    
    test("var t;var AB=function(){return 4};" +
         "function BC(){return 6;}" +
         "CD=function(x){return x + 5};x=CD(3);y=AB();z=BC();",
         "var t;CD=function(x){return x+5};x=CD(3);y=4;z=6"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions3
  public void testInlineFunctions3() {
    
    test("var t;var AB=function(){return 4};" +
        "function BC(){return 6;}" +
        "var CD=function(x){return x + 5};x=CD(3);y=AB();z=BC();",
        "var t;x=3+5;y=4;z=6");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions4
  public void testInlineFunctions4() {
    
    test("var t; var AB = function() { return 4 }; " +
        "function BC() { return 6; }" +
        "CD = 0;" +
        "CD = function(x) { return x + 5 }; x = CD(3); y = AB(); z = BC();",

        "var t;CD=0;CD=function(x){return x+5};x=CD(3);y=4;z=6");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions5
  public void testInlineFunctions5() {
    
    test("var FOO_FN=function(x,y) { return \"de\" + x + \"nu\" + y };" +
         "var a = FOO_FN(\"ez\", \"ts\")",

         "var a=\"de\"+\"ez\"+\"nu\"+\"ts\"");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions6
  public void testInlineFunctions6() {
    
    test("function BAR_FN(x, y, z) { return z(foo(x + y)) }" +
         "alert(BAR_FN(1, 2, baz))",

         "alert(baz(foo(1+2)))");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions7
  public void testInlineFunctions7() {
    
    test("function FN(x,y,z){return x+x+y}" +
         "var b=FN(1,2,3)",

         "var b=1+1+2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions8
  public void testInlineFunctions8() {
    
    test("function MUL(x,y){return x*y}function ADD(x,y){return x+y}" +
         "var a=1+MUL(2,3);var b=2*ADD(3,4)",

         "var a=1+2*3;var b=2*(3+4)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions9
  public void testInlineFunctions9() {
    
    test("function INC(x){return x++}" +
         "var y=INC(i)",
         "var y;{var x$$inline_0=i;" +
         "y=x$$inline_0++}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions10
  public void testInlineFunctions10() {
    test("function INC(x){return x++}" +
         "var y=INC(i);y=INC(i)",
         "var y;" +
         "{var x$$inline_0=i;" +
         "y=x$$inline_0++}" +
         "{var x$$inline_2=i;" +
         "y=x$$inline_2++}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions11
  public void testInlineFunctions11() {
    test("function f(x){return x}" +
          "var y=f(i)",
          "var y=i");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions12
  public void testInlineFunctions12() {
    
    allowBlockInlining = false;
    test("function f(x){return x}" +
          "var y=f(i)",
          "var y=i");
    testSame("function f(x){return x}" +
         "var y=f(i++)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions13
  public void testInlineFunctions13() {
    
    test("function f(x){return x}" +
         "var y=f(i++)",
         "var y;{var x$$inline_0=i++;y=x$$inline_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions14
  public void testInlineFunctions14() {
    
    test("function FOO(x){return x}var BAR=function(y){return y}" +
             ";b=FOO;a(BAR);x=FOO(1);y=BAR(2)",

         "function FOO(x){return x}var BAR=function(y){return y}" +
             ";b=FOO;a(BAR);x=1;y=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15a
  public void testInlineFunctions15a() {
    
    test("function foo(){return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "var d=b()+foo()",

         "var d=c+function(a){return a+1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15b
  public void testInlineFunctions15b() {
    assumeMinimumCapture = false;

    
    test("function foo(){var x;return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "var d=b()+foo()",

         "function foo(){var x;return function(a){return a+1}}" +
         "var d=c+foo()");

    assumeMinimumCapture = true;

    test("function foo(){var x;return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "var d=b()+foo()",

         "var JSCompiler_temp_const$$0 = c;\n" +
         "{\n" +
         "var JSCompiler_inline_result$$1;\n" +
         "var x$$inline_2;\n" +
         "JSCompiler_inline_result$$1 = " +
         "    function(a$$inline_3){ return a$$inline_3+1 };\n" +
         "}" +
         "var d=JSCompiler_temp_const$$0 + JSCompiler_inline_result$$1");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15c
  public void testInlineFunctions15c() {
    assumeMinimumCapture = false;

    
    test("function foo(){return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "function _x(){ var d=b()+foo() }",

         "function foo(){return function(a){return a+1}}" +
         "function _x(){ var d=c+foo() }");

    assumeMinimumCapture = true;

    
    test("function foo(){return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "function _x(){ var d=b()+foo() }",

         "function _x(){var d=c+function(a){return a+1}}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15d
  public void testInlineFunctions15d() {
    assumeMinimumCapture = false;

    
    test("function foo(){var x; return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "function _x(){ var d=b()+foo() }",

         "function foo(){var x; return function(a){return a+1}}" +
         "function _x(){ var d=c+foo() }");

    assumeMinimumCapture = true;

    
    test("function foo(){var x; return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "function _x(){ var d=b()+foo() }",

         "function _x() { \n" +
         "  var JSCompiler_temp_const$$0 = c;\n" +
         "  {\n" +
         "  var JSCompiler_inline_result$$1;\n" +
         "  var x$$inline_2;\n" +
         "  JSCompiler_inline_result$$1 = " +
         "      function(a$$inline_3) {return a$$inline_3+1};\n" +
         "  }\n" +
         "  var d = JSCompiler_temp_const$$0+JSCompiler_inline_result$$1\n" +
         "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions16a
  public void testInlineFunctions16a() {
    assumeMinimumCapture = false;

    testSame("function foo(b){return window.bar(function(){c(b)})}" +
         "var d=foo(e)");

    assumeMinimumCapture = true;

    test(
        "function foo(b){return window.bar(function(){c(b)})}" +
        "var d=foo(e)",
        "var d;{var b$$inline_0=e;" +
        "d=window.bar(function(){c(b$$inline_0)})}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions16b
  public void testInlineFunctions16b() {
    test("function foo(){return window.bar(function(){c()})}" +
         "var d=foo(e)",
         "var d=window.bar(function(){c()})");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions17
  public void testInlineFunctions17() {
    
    testSame("function foo(x){return x*x+foo(3)}var bar=foo(4)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions18
  public void testInlineFunctions18() {
    
    allowBlockInlining = false;
    test("function foo(a, b){return a+b}" +
         "function bar(d){return c}" +
         "var d=foo(bar(1),e)",
         "var d=c+e");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions19
  public void testInlineFunctions19() {
    
    
    test("function foo(a, b){return a+b}" +
        "function bar(d){return c}" +
        "var d=foo(bar(1),e)",
        "var d;{d=c+e}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions20
  public void testInlineFunctions20() {
    
    allowBlockInlining = false;
    test("function foo(a, b){return a+b}" +
         "function bar(d){return c}" +
         "var d=bar(foo(1,e));",
         "var d=c");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions21
  public void testInlineFunctions21() {
    
    test("function foo(a, b){return a+b}" +
        "function bar(d){return c}" +
        "var d=bar(foo(1,e))",
        "var d;{d=c}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions22
  public void testInlineFunctions22() {
    
    test("function plex(a){if(a) return 0;else return 1;}" +
         "function foo(a, b){return bar(a+b)}" +
         "function bar(d){return plex(d)}" +
         "var d=foo(1,2)",

         "var d;{JSCompiler_inline_label_plex_1:{" +
         "if(1+2){" +
         "d=0;break JSCompiler_inline_label_plex_1}" +
         "else{" +
         "d=1;break JSCompiler_inline_label_plex_1}d=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions23
  public void testInlineFunctions23() {
    
    test("function complex(a){if(a) return 0;else return 1;}" +
         "function bar(d){return complex(d)}" +
         "function foo(a, b){return bar(a+b)}" +
         "var d=foo(1,2)",

         "var d;{JSCompiler_inline_label_complex_1:{" +
         "if(1+2){" +
         "d=0;break JSCompiler_inline_label_complex_1" +
         "}else{" +
         "d=1;break JSCompiler_inline_label_complex_1" +
         "}d=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions24
  public void testInlineFunctions24() {
    
    testSame("function foo(x){return this}foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions25
  public void testInlineFunctions25() {
    testSame("function foo(){return arguments[0]}foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions26
  public void testInlineFunctions26() {
    
    testSame("function _foo(x){return x}_foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions27
  public void testInlineFunctions27() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {y: 1, z: foo(2)};",
        "var window={};" +
        "{" +
        "  var JSCompiler_inline_result$$0;" +
        "  window.bar++;" +
        "  JSCompiler_inline_result$$0 = 3;" +
        "}" +
        "var x = {y: 1, z: JSCompiler_inline_result$$0};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions28
  public void testInlineFunctions28() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {y: alert(), z: foo(2)};",
        "var window = {};" +
        "var JSCompiler_temp_const$$0 = alert();" +
        "{" +
        " var JSCompiler_inline_result$$1;" +
        " window.bar++;" +
        " JSCompiler_inline_result$$1 = 3;}" +
        "var x = {" +
        "  y: JSCompiler_temp_const$$0," +
        "  z: JSCompiler_inline_result$$1" +
        "};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions29
  public void testInlineFunctions29() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {a: alert(), b: alert2(), c: foo(2)};",
        "var window = {};" +
        "var JSCompiler_temp_const$$1 = alert();" +
        "var JSCompiler_temp_const$$0 = alert2();" +
        "{" +
        " var JSCompiler_inline_result$$2;" +
        " window.bar++;" +
        " JSCompiler_inline_result$$2 = 3;}" +
        "var x = {" +
        "  a: JSCompiler_temp_const$$1," +
        "  b: JSCompiler_temp_const$$0," +
        "  c: JSCompiler_inline_result$$2" +
        "};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions30
  public void testInlineFunctions30() {
    
    testSame("function foo(){ return eval() }" +
        "foo();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions31
  public void testInlineFunctions31() {
    
    test("function foo(){ lab:{4;} }" +
        "lab:{foo();}",
        "lab:{{JSCompiler_inline_label_0:{4}}}");
  }
