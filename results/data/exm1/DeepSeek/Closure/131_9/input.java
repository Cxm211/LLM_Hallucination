// buggy code
    public static boolean isJSIdentifier(String s) {
      int length = s.length();

      if (length == 0 ||
          !Character.isJavaIdentifierStart(s.charAt(0))) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        if (
            !Character.isJavaIdentifierPart(s.charAt(i))) {
          return false;
        }
      }

      return true;
    }

// relevant test
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

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testDefine
  public void testDefine() {
    test("define(['foo', 'bar'], function(foo, bar) { foo(bar); bar+1; })",
        "var foo=require('foo'); var bar=require('bar');foo(bar);bar+1");
    test("define(['foo', 'bar'], function(foo, bar, baz) { foo(bar); bar+1; })",
        "var foo=require('foo'); var bar=require('bar');" +
        "var baz = null;foo(bar);bar+1");
    test("define(['foo', 'bar'], function(foo, bar) { return { test: 1 } })",
        "var foo=require('foo'); var bar=require('bar');" +
        "module.exports={test:1}");
    test("define(['foo', 'bar'], function(foo, bar, exports) { " +
        "return { test: 1 } })",
        "var foo=require('foo'); var bar=require('bar');" +
        "module.exports={test:1}");
    test("define(['foo', 'bar'], function(foo, bar, exports, module) { " +
        "return { test: 1 } })",
        "var foo=require('foo'); var bar=require('bar');" +
        "module.exports={test:1}");
    test("define(['foo', 'bar'], function(foo, bar, exports, module, baz) { " +
        "return { test: 1 } })",
        "var foo=require('foo'); var bar=require('bar');var baz = null;" +
        "module.exports={test:1}");
    test("define(['foo', 'bar'], function(foo) { return { test: 1 } })",
        "var foo=require('foo'); require('bar'); module.exports={test:1}");
    test("define(['foo', 'bar'], function(test) { return { test: 1 } })",
        "var test=require('foo'); require('bar'); module.exports={test:1}");
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testVarRenaming
  public void testVarRenaming() {
    final String suffix = TransformAMDToCJSModule.VAR_RENAME_SUFFIX;
    test("var foo; define(['foo', 'bar'], function(foo, bar) { " +
        "foo(bar); bar+1; })",
        "var foo; var foo" + suffix +"0=require('foo');" +
        "var bar=require('bar');foo" + suffix +"0(bar);bar+1");
    test("function foo() {}; define(['foo', 'bar'], " +
        "function(foo, bar) { foo(bar); bar+1; })",
        "function foo() {}; var foo" + suffix +"0=require('foo'); " +
        "var bar=require('bar');foo" + suffix +"0(bar);bar+1");
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testDefineOnlyFunction
  public void testDefineOnlyFunction() {
    test("define(function() { return { test: 1 } })",
        "module.exports={test:1}");
    test("define(function(exports, module) { return { test: 1 } })",
        "module.exports={test:1}");
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testObjectLit
  public void testObjectLit() {
    test("define({foo: 'bar'})", "exports={foo: 'bar'}");
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testPlugins
  public void testPlugins() {
    test("define(['foo', 'text!foo'], function(foo, text) {})",
        "var foo = require('foo'); var text = null;",
        null, TransformAMDToCJSModule.REQUIREJS_PLUGINS_NOT_SUPPORTED_WARNING);
    test("define(['foo', 'text!foo?bar'], function(foo, bar) {})",
        "var foo = require('foo'); var bar = require('bar'); ",
        null, TransformAMDToCJSModule.REQUIREJS_PLUGINS_NOT_SUPPORTED_WARNING);
    test("define(['foo', 'text!foo?:bar'], function(foo, bar) {})",
        "var foo = require('foo'); var bar = null;",
        null, TransformAMDToCJSModule.REQUIREJS_PLUGINS_NOT_SUPPORTED_WARNING);
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testUnsupportedForms
  public void testUnsupportedForms() {
    testUnsupported("define()");
    testUnsupported("define([], function() {}, 1)");
    testUnsupported("define({}, function() {})");
    testUnsupported("define('test', function() {})");
    testUnsupported("define([])");
    testUnsupported("define(true)");
    testUnsupported("define(1)");
    testNonTopLevelDefine("var x = define(function() {});");
    testNonTopLevelDefine("if(define(function() {})) {}");
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testLocalDefine
  public void testLocalDefine() {
    testSame("(function() { function define() {}; define({}); })()");
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubProperty
  public void testStubProperty() {
    testSame("function Foo() {}; Foo.bar;");
    ObjectType foo = (ObjectType) globalScope.getVar("Foo").getType();
    assertFalse(foo.hasProperty("bar"));
    Asserts.assertTypeEquals(registry.getNativeType(UNKNOWN_TYPE),
        foo.getPropertyType("bar"));
    Asserts.assertTypeCollectionEquals(
        Lists.newArrayList(foo), registry.getTypesWithProperty("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorProperty
  public void testConstructorProperty() {
    testSame("var foo = {};  foo.Bar = function() {};");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("Bar"));
    assertFalse(foo.isPropertyTypeInferred("Bar"));

    JSType fooBar = foo.getPropertyType("Bar");
    assertEquals("function (new:foo.Bar): undefined", fooBar.toString());
    Asserts.assertTypeCollectionEquals(
        Lists.newArrayList(foo), registry.getTypesWithProperty("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPrototypePropertyMethodWithoutAnnotation
  public void testPrototypePropertyMethodWithoutAnnotation() {
    testSame("var Foo = function Foo() {};" +
             "var proto = Foo.prototype = {" +
             "   bar: function(a, b){}" +
             "};" +
             "proto.baz = function(c) {};" +
             "(function() { proto.baz = function() {}; })();");
    ObjectType foo = (ObjectType) findNameType("Foo", globalScope);
    assertTrue(foo.hasProperty("prototype"));

    ObjectType fooProto = (ObjectType) foo.getPropertyType("prototype");
    assertTrue(fooProto.hasProperty("bar"));
    assertEquals("function (?, ?): undefined",
        fooProto.getPropertyType("bar").toString());

    assertTrue(fooProto.hasProperty("baz"));
    assertEquals("function (?): undefined",
        fooProto.getPropertyType("baz").toString());
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
    Asserts.assertTypeCollectionEquals(
        Lists.newArrayList(foo), registry.getTypesWithProperty("Bar"));
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

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBogusPrototypeInit
  public void testBogusPrototypeInit() {
    
    testSame(" var goog = {}; " +
        "goog.F = {};  goog.F.prototype = {};" +
        " goog.F = function() {};");
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

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnumElement
  public void testEnumElement() {
    testSame(" var Foo = {BAR: 1}; var f = Foo;");
    Var bar = globalScope.getVar("Foo.BAR");
    assertNotNull(bar);
    assertEquals("Foo.<number>", bar.getType().toString());
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
    Asserts.assertTypeEquals(registry.getType("FooAlias"),
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
    Asserts.assertTypeEquals(registry.getType("goog.Foo"),
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

    Asserts.assertTypeEquals(globalScope.getVar("goog.foo").getType(),
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

    Asserts.assertTypeEquals(lastLocalScope.getVar("goog.foo").getType(),
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
    Asserts.assertTypeEquals(getNativeType(NUMBER_TYPE),
        instanceType.getPropertyType("m1"));
    Asserts.assertTypeEquals(getNativeType(BOOLEAN_TYPE),
        instanceType.getPropertyType("m2"));
    Asserts.assertTypeEquals(getNativeType(STRING_TYPE),
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

    Asserts.assertTypeEquals(iPrototype, globalScope.getVar("I.prototype").getType());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertiesOnInterface2
  public void testPropertiesOnInterface2() throws Exception {
    testSame(" var I = function() {};" +
        "I.prototype = {baz: function(){}};" +
        " I.prototype.bar;");

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

    
    assertNull(globalScope.getVar("I.prototype"));
    
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns
  public void testStubsInExterns() {
    testSame(
        " function Extern() {}" +
        "Extern.prototype.bar;" +
        "var e = new Extern(); e.baz;",
        " function Foo() {}" +
        "Foo.prototype.bar;" +
        "var f = new Foo(); f.baz;", null);

    ObjectType e = (ObjectType) globalScope.getVar("e").getType();
    assertEquals("?", e.getPropertyType("bar").toString());
    assertEquals("?", e.getPropertyType("baz").toString());

    ObjectType f = (ObjectType) globalScope.getVar("f").getType();
    assertEquals("?", f.getPropertyType("bar").toString());
    assertFalse(f.hasProperty("baz"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns2
  public void testStubsInExterns2() {
    testSame(
        " function Extern() {}" +
        " var myExtern;" +
        " myExtern.foo;",
        "", null);

    JSType e = globalScope.getVar("myExtern").getType();
    assertEquals("(Extern|null)", e.toString());

    ObjectType externType = (ObjectType) e.restrictByNotNullOrUndefined();
    assertTrue(globalScope.getRootNode().toStringTree(),
        externType.hasOwnProperty("foo"));
    assertTrue(externType.isPropertyTypeDeclared("foo"));
    assertEquals("number", externType.getPropertyType("foo").toString());
    assertTrue(externType.isPropertyInExterns("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns3
  public void testStubsInExterns3() {
    testSame(
        " myExtern.foo;" +
        " var myExtern;" +
        " function Extern() {}",
        "", null);

    JSType e = globalScope.getVar("myExtern").getType();
    assertEquals("(Extern|null)", e.toString());

    ObjectType externType = (ObjectType) e.restrictByNotNullOrUndefined();
    assertTrue(globalScope.getRootNode().toStringTree(),
        externType.hasOwnProperty("foo"));
    assertTrue(externType.isPropertyTypeDeclared("foo"));
    assertEquals("number", externType.getPropertyType("foo").toString());
    assertTrue(externType.isPropertyInExterns("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns4
  public void testStubsInExterns4() {
    testSame(
        "Extern.prototype.foo;" +
        " function Extern() {}",
        "", null);

    JSType e = globalScope.getVar("Extern").getType();
    assertEquals("function (new:Extern): ?", e.toString());

    ObjectType externProto = ((FunctionType) e).getPrototype();
    assertTrue(globalScope.getRootNode().toStringTree(),
        externProto.hasOwnProperty("foo"));
    assertTrue(externProto.isPropertyTypeInferred("foo"));
    assertEquals("?", externProto.getPropertyType("foo").toString());
    assertTrue(externProto.isPropertyInExterns("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyInExterns1
  public void testPropertyInExterns1() {
    testSame(
        " function Extern() {}" +
        " var extern;" +
        " extern.one;",
        " function Normal() {}" +
        " var normal;" +
        " normal.one;", null);

    JSType e = globalScope.getVar("Extern").getType();
    ObjectType externInstance = ((FunctionType) e).getInstanceType();
    assertTrue(externInstance.hasOwnProperty("one"));
    assertTrue(externInstance.isPropertyTypeDeclared("one"));
    assertEquals("function (): number",
        externInstance.getPropertyType("one").toString());

    JSType n = globalScope.getVar("Normal").getType();
    ObjectType normalInstance = ((FunctionType) n).getInstanceType();
    assertFalse(normalInstance.hasOwnProperty("one"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyInExterns2
  public void testPropertyInExterns2() {
    testSame(
        " var extern;" +
        " extern.one;",
        " var normal;" +
        " normal.one;", null);

    JSType e = globalScope.getVar("extern").getType();
    assertFalse(e.dereference().hasOwnProperty("one"));

    JSType normal = globalScope.getVar("normal").getType();
    assertFalse(normal.dereference().hasOwnProperty("one"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyInExterns3
  public void testPropertyInExterns3() {
    testSame(
        " function Object(x) {}" +
        " Object.one;", "", null);

    ObjectType obj = globalScope.getVar("Object").getType().dereference();
    assertTrue(obj.hasOwnProperty("one"));
    assertEquals("number", obj.getPropertyType("one").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTypedStubsInExterns
  public void testTypedStubsInExterns() {
    testSame(
        " " +
        "function Function(var_args) {}" +
        " Function.prototype.apply;",
        "var f = new Function();", null);

    ObjectType f = (ObjectType) globalScope.getVar("f").getType();

    
    
    assertEquals(
        "function (?=, (Object|null)=): ?",
        f.getPropertyType("apply").toString());

    
    
    FunctionType func = (FunctionType) globalScope.getVar("Function").getType();
    assertEquals("Function",
        func.getPrototype().getPropertyType("apply").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTypesInExterns
  public void testTypesInExterns() throws Exception {
    testSame(
        CompilerTypeTestCase.DEFAULT_EXTERNS,
        "", null);

    Var v = globalScope.getVar("Object");
    FunctionType obj = (FunctionType) v.getType();
    assertEquals("function (new:Object, *=): ?", obj.toString());
    assertNotNull(v.getNode());
    assertNotNull(v.input);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyDeclarationOnInstanceType
  public void testPropertyDeclarationOnInstanceType() {
    testSame(
        " var a = {};" +
        " a.name = 0;");

    assertEquals("number", globalScope.getVar("a.name").getType().toString());

    ObjectType a = (ObjectType) (globalScope.getVar("a").getType());
    assertFalse(a.hasProperty("name"));
    assertFalse(getNativeObjectType(OBJECT_TYPE).hasProperty("name"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyDeclarationOnRecordType
  public void testPropertyDeclarationOnRecordType() {
    testSame(
        " var a = {foo: 3};" +
        " a.name = 0;");

    assertEquals("number", globalScope.getVar("a.name").getType().toString());

    ObjectType a = (ObjectType) (globalScope.getVar("a").getType());
    assertEquals("{foo: number}", a.toString());
    assertFalse(a.hasProperty("name"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalThis1
  public void testGlobalThis1() {
    testSame(
        " function Window() {}" +
        "Window.prototype.alert = function() {};" +
        "var x = this;");

    ObjectType x = (ObjectType) (globalScope.getVar("x").getType());
    FunctionType windowCtor =
        (FunctionType) (globalScope.getVar("Window").getType());
    assertEquals("global this", x.toString());
    assertTrue(x.isSubtype(windowCtor.getInstanceType()));
    assertFalse(x.isEquivalentTo(windowCtor.getInstanceType()));
    assertTrue(x.hasProperty("alert"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalThis2
  public void testGlobalThis2() {
    testSame(
        " function Window() {}" +
        "Window.prototype = {alert: function() {}};" +
        "var x = this;");

    ObjectType x = (ObjectType) (globalScope.getVar("x").getType());
    FunctionType windowCtor =
        (FunctionType) (globalScope.getVar("Window").getType());
    assertEquals("global this", x.toString());
    assertTrue(x.isSubtype(windowCtor.getInstanceType()));
    assertFalse(x.isEquivalentTo(windowCtor.getInstanceType()));
    assertTrue(x.hasProperty("alert"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testObjectLiteralCast
  public void testObjectLiteralCast() {
    
    
    testSame(" A.B = function() {}\n" +
             "A.B.prototype.isEnabled = true;\n" +
             "goog.reflect.object(A.B, {isEnabled: 3})\n" +
             "var x = (new A.B()).isEnabled;");

    assertEquals("A.B",
        findTokenType(Token.OBJECTLIT, globalScope).toString());
    assertEquals("boolean",
        findNameType("x", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadObjectLiteralCast1
  public void testBadObjectLiteralCast1() {
    testSame(" A.B = function() {}\n" +
             "goog.reflect.object(A.B, 1)",
             ClosureCodingConvention.OBJECTLIT_EXPECTED);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadObjectLiteralCast2
  public void testBadObjectLiteralCast2() {
    testSame("goog.reflect.object(A.B, {})",
             TypedScopeCreator.CONSTRUCTOR_EXPECTED);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorNode
  public void testConstructorNode() {
    testSame("var goog = {};  goog.Foo = function() {};");

    ObjectType ctor = (ObjectType) (findNameType("goog.Foo", globalScope));
    assertNotNull(ctor);
    assertTrue(ctor.isConstructor());
    assertEquals("function (new:goog.Foo): undefined", ctor.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testForLoopIntegration
  public void testForLoopIntegration() {
    testSame("var y = 3; for (var x = true; x; y = x) {}");

    Var y = globalScope.getVar("y");
    assertTrue(y.isTypeInferred());
    assertEquals("(boolean|number)", y.getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorAlias
  public void testConstructorAlias() {
    testSame(
        " var Foo = function() {};" +
        " var FooAlias = Foo;");
    assertEquals("Foo", registry.getType("FooAlias").toString());
    Asserts.assertTypeEquals(registry.getType("Foo"), registry.getType("FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedConstructorAlias
  public void testNamespacedConstructorAlias() {
    testSame(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        " goog.FooAlias = goog.Foo;");
    assertEquals("goog.Foo", registry.getType("goog.FooAlias").toString());
    Asserts.assertTypeEquals(registry.getType("goog.Foo"),
        registry.getType("goog.FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType1
  public void testTemplateType1() {
    testSame(
        "\n" +
        "function bind(fn, thisObj) {}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "Foo.prototype.baz = function() {};\n" +
        "bind(function() { var g = this; var f = this.baz(); }, new Foo());");
    assertEquals("Foo", findNameType("g", lastLocalScope).toString());
    assertEquals("number", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType2
  public void testTemplateType2() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var val = 'hi';\n" +
        "var result = f(val);");
    assertEquals("string", findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType2a
  public void testTemplateType2a() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var val = 'hi';\n" +
        "var result = f(val);");
    assertEquals("(string|undefined)",
        findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType2b
  public void testTemplateType2b() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var val = 'hi';\n" +
        "var result = f(val);");
    assertEquals("(string|undefined)",
        findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType3
  public void testTemplateType3() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var val1 = 'hi';\n" +
        "var result1 = f(val1);" +
        "\n" +
        "var val2 = 0;\n" +
        "var result2 = f(val2);");

    assertEquals("string", findNameType("result1", globalScope).toString());
    assertEquals("number", findNameType("result2", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType4
  public void testTemplateType4() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var arr = [];\n" +
        "(function () {var result = f(arr);})();");

    JSType resultType = findNameType("result", lastLocalScope);
    assertEquals("Array.<string>", resultType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType4a
  public void testTemplateType4a() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var g = function(){return 'hi'};\n" +
        "(function () {var result = f(g);})();");

    JSType resultType = findNameType("result", lastLocalScope);
    assertEquals("string", resultType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType4b
  public void testTemplateType4b() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var g = function(x){};\n" +
        "(function () {var result = f(g);})();");

    JSType resultType = findNameType("result", lastLocalScope);
    assertEquals("string", resultType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType5
  public void testTemplateType5() {
    testSame(
        "\n" +
        "function f(arr) {\n" +
        "  return arr;\n" +
        "}" +
        "\n" +
        "var arr = [];\n" +
        "var result = f(arr);");

    assertEquals("Array.<string>", findNameTypeStr("result", globalScope));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType6
  public void testTemplateType6() {
    testSame(
        "\n" +
        "function f(arr) {\n" +
        "  return arr;\n" +
        "}" +
        "\n" +
        "var arr = [];\n" +
        "var result = f(arr);");

    assertEquals("Array.<string>", findNameTypeStr("result", globalScope));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType7
  public void testTemplateType7() {
    testSame(
        "var goog = {};\n" +
        "goog.array = {};\n" +
        "\n" +
        "goog.array.filter = function(arr, f, opt_obj) {\n" +
        "  var res = [];\n" +
        "  for (var i = 0; i < arr.length; i++) {\n" +
        "     if (f.call(opt_obj, arr[i], i, arr)) {\n" +
        "        res.push(val);\n" +
        "     }\n" +
        "  }\n" +
        "  return res;\n" +
        "}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "var arr = [];\n" +
        "var result = goog.array.filter(arr," +
        "  function(a,b,c) {var self=this;}, new Foo());");

    assertEquals("Foo", findNameType("self", lastLocalScope).toString());
    assertEquals("string", findNameType("a", lastLocalScope).toString());
    assertEquals("number", findNameType("b", lastLocalScope).toString());
    assertEquals("Array.<string>",
        findNameType("c", lastLocalScope).toString());
    assertEquals("Array.<string>",
        findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType7b
  public void testTemplateType7b() {
    testSame(
        "var goog = {};\n" +
        "goog.array = {};\n" +
        "\n" +
        "goog.array.filter = function(arr, f, opt_obj) {\n" +
        "  var res = [];\n" +
        "  for (var i = 0; i < arr.length; i++) {\n" +
        "     if (f.call(opt_obj, arr[i], i, arr)) {\n" +
        "        res.push(val);\n" +
        "     }\n" +
        "  }\n" +
        "  return res;\n" +
        "}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "var arr = [];\n" +
        "var result = goog.array.filter(arr," +
        "  function(a,b,c) {var self=this;}, new Foo());");

    assertEquals("Foo", findNameType("self", lastLocalScope).toString());
    assertEquals("string", findNameType("a", lastLocalScope).toString());
    assertEquals("number", findNameType("b", lastLocalScope).toString());
    assertEquals("Array.<string>",
        findNameType("c", lastLocalScope).toString());
    assertEquals("Array.<string>",
        findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType7c
  public void testTemplateType7c() {
    testSame(
        "var goog = {};\n" +
        "goog.array = {};\n" +
        "\n" +
        "goog.array.filter = function(arr, f, opt_obj) {\n" +
        "  var res = [];\n" +
        "  for (var i = 0; i < arr.length; i++) {\n" +
        "     if (f.call(opt_obj, arr[i], i, arr)) {\n" +
        "        res.push(val);\n" +
        "     }\n" +
        "  }\n" +
        "  return res;\n" +
        "}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "var arr = [];\n" +
        "var result = goog.array.filter(arr," +
        "  function(a,b,c) {var self=this;}, new Foo());");

    assertEquals("Foo", findNameType("self", lastLocalScope).toString());
    assertEquals("string", findNameType("a", lastLocalScope).toString());
    assertEquals("number", findNameType("b", lastLocalScope).toString());
    assertEquals("(Array.<string>|null)",
        findNameType("c", lastLocalScope).toString());
    assertEquals("Array.<string>",
        findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType9
  public void testTemplateType9() {
    testSame(
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "Foo.prototype.method = function() {};\n" +
        "\n" +
        "function Bar() {}\n" +
        "\n" +
        "var g = new Bar().method();\n");
    assertEquals("Bar", findNameType("g", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType10
  public void testTemplateType10() {
    
    
    
    testSame(
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "\n" +
        "Foo.prototype.method = function() {var g = this;};\n");
    assertEquals("T", findNameType("g", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType11
  public void testTemplateType11() {
    testSame(
        "\n" +
        "var method = function() {};\n" +
        "\n" +
        "function Bar() {}\n" +
        "\n" +
        "var g = method().call(new Bar());\n");
    
    assertEquals("?", findNameType("g", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType12
  public void testTemplateType12() {
    testSame(
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "\n" +
        "Foo.prototype.method = function() {var g = this;};\n");
    assertEquals("(Array.<T>|{length: number})",
        findNameType("g", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClosureParameterTypesWithoutJSDoc
  public void testClosureParameterTypesWithoutJSDoc() {
    testSame(
        "\n" +
        "function foo(bar) {}\n" +
        "foo(function(baz) { var f = baz; })\n");
    assertEquals("Object", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClosureParameterTypesWithJSDoc
  public void testClosureParameterTypesWithJSDoc() {
    testSame(
        "\n" +
        "function foo(bar) {}\n" +
        "foo(" +
        "  (function(baz) { var f = baz; }))\n");
    assertEquals("string", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDuplicateExternProperty1
  public void testDuplicateExternProperty1() {
    testSame(
        " function Foo() {}" +
        "Foo.prototype.bar;" +
        " Foo.prototype.bar; var x = (new Foo).bar;",
        null);
    assertEquals("number", findNameType("x", globalScope).toString());
  }

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

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod4
  public void testAbstractMethod4() {
    testSame(
        " var abstractMethod;" +
        " function Foo() {}" +
        "Foo.prototype = { bar: abstractMethod};");
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

// com.google.javascript.jscomp.TypedScopeCreatorTest::testActiveXObject
  public void testActiveXObject() {
    testSame(
        CompilerTypeTestCase.ACTIVE_X_OBJECT_DEF,
        "var x = new ActiveXObject();", null);
    assertEquals(
        "?",
        findNameType("x", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference1
  public void testReturnTypeInference1() {
    testSame("function f() {}");
    assertEquals(
        "function (): undefined",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference2
  public void testReturnTypeInference2() {
    testSame(" function f() {}");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference3
  public void testReturnTypeInference3() {
    testSame("function f() {x: return 3;}");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference4
  public void testReturnTypeInference4() {
    testSame("function f() { throw Error(); }");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference5
  public void testReturnTypeInference5() {
    testSame("function f() { if (true) { return 1; } }");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testLiteralTypesInferred
  public void testLiteralTypesInferred() {
    testSame("null + true + false + 0 + '' + {}");
    assertEquals(
        "null", findTokenType(Token.NULL, globalScope).toString());
    assertEquals(
        "boolean", findTokenType(Token.TRUE, globalScope).toString());
    assertEquals(
        "boolean", findTokenType(Token.FALSE, globalScope).toString());
    assertEquals(
        "number", findTokenType(Token.NUMBER, globalScope).toString());
    assertEquals(
        "string", findTokenType(Token.STRING, globalScope).toString());
    assertEquals(
        "{}", findTokenType(Token.OBJECTLIT, globalScope).toString());
  }

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
