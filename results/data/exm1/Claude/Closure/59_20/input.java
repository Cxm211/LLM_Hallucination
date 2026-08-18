// buggy code
  public void initOptions(CompilerOptions options) {
    this.options = options;
    if (errorManager == null) {
      if (outStream == null) {
        setErrorManager(
            new LoggerErrorManager(createMessageFormatter(), logger));
      } else {
        PrintStreamErrorManager printer =
            new PrintStreamErrorManager(createMessageFormatter(), outStream);
        printer.setSummaryDetailLevel(options.summaryDetailLevel);
        setErrorManager(printer);
      }
    }

    // DiagnosticGroups override the plain checkTypes option.
    if (options.enables(DiagnosticGroups.CHECK_TYPES)) {
      options.checkTypes = true;
    } else if (options.disables(DiagnosticGroups.CHECK_TYPES)) {
      options.checkTypes = false;
    } else if (!options.checkTypes) {
      // If DiagnosticGroups did not override the plain checkTypes
      // option, and checkTypes is enabled, then turn off the
      // parser type warnings.
      options.setWarningLevel(
          DiagnosticGroup.forType(
              RhinoErrorReporter.TYPE_PARSE_ERROR),
          CheckLevel.OFF);
    }

    if (options.checkGlobalThisLevel.isOn()) {
      options.setWarningLevel(
          DiagnosticGroups.GLOBAL_THIS,
          options.checkGlobalThisLevel);
    }

    if (options.getLanguageIn() == LanguageMode.ECMASCRIPT5_STRICT) {
      options.setWarningLevel(
          DiagnosticGroups.ES5_STRICT,
          CheckLevel.ERROR);
    }

    // Initialize the warnings guard.
    List<WarningsGuard> guards = Lists.newArrayList();
    guards.add(
        new SuppressDocWarningsGuard(
            getDiagnosticGroups().getRegisteredGroups()));
    guards.add(options.getWarningsGuard());

    ComposeWarningsGuard composedGuards = new ComposeWarningsGuard(guards);

    // All passes must run the variable check. This synthesizes
    // variables later so that the compiler doesn't crash. It also
    // checks the externs file for validity. If you don't want to warn
    // about missing variable declarations, we shut that specific
    // error off.
    if (!options.checkSymbols &&
        !composedGuards.enables(DiagnosticGroups.CHECK_VARIABLES)) {
      composedGuards.addGuard(new DiagnosticGroupWarningsGuard(
          DiagnosticGroups.CHECK_VARIABLES, CheckLevel.OFF));
    }

    this.warningsGuard = composedGuards;
  }

// relevant test
// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testThrow
  public void testThrow() throws Exception {
    testConversion("throw e");
    testConversion("throw 2 + 3 * 4");
    testConversion("throw (function () {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}})()");
    testConversion("throw f(x)");
    testConversion("throw f(f(f(x)))");
    testConversion("throw (f(f(x), y))()");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testTry
  public void testTry() throws Exception {
    testConversion("try {} catch (e) {}");
    testConversion("try {;} catch (e) {;}");
    testConversion("try {var x = 0; y / x} catch (e) {f(e)}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; "
        + "h(q)} catch (e) {f(x)}");

    testConversion("try {} finally {}");
    testConversion("try {;} finally {;}");
    testConversion("try {var x = 0; y / x} finally {f(y)}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; "
        + "h(q)} finally {f(x)}");

    testConversion("try {} catch (e) {} finally {}");
    testConversion("try {;} catch (e) {;} finally {;}");
    testConversion("try {var x = 0; y / x} catch (e) {;} finally {;}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; h(q)} "
        + "catch (e) {f(x)} finally {f(x)}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testTypeof
  public void testTypeof() throws Exception {
    testConversion("typeof undefined");
    testConversion("typeof null");
    testConversion("typeof 1");
    testConversion("typeof 'a'");
    testConversion("typeof false");

    testConversion("typeof Null()");
    testConversion("typeof Number(1)");
    testConversion("typeof String('a')");
    testConversion("typeof Boolean(0)");

    testConversion("typeof x");
    testConversion("typeof new A()");
    testConversion("typeof new A(x)");
    testConversion("typeof f(x)");
    testConversion("typeof (function() {})()");
    testConversion("typeof 2 + 3 * 4");

    testConversion("typeof typeof x");
    testConversion("typeof typeof typeof x");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testUnaryExpressions
  public void testUnaryExpressions() throws Exception {
    testConversion("!x");
    testConversion("!null");
    testConversion("!3.14");
    testConversion("!true");

    testConversion("~x");
    testConversion("~null");
    testConversion("~3.14");
    testConversion("~true");

    testConversion("+x");
    testConversion("+null");
    testConversion("+3.14");
    testConversion("+true");

    testConversion("-x");
    testConversion("-null");
    testConversion("-true");

    testConversion("!~+-z");
    testConversion("void x");
    testConversion("void null");
    testConversion("void void !x");
    testConversion("void (x + 1)");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testVarDeclarations
  public void testVarDeclarations() throws Exception {
    testConversion("var x");
    testConversion("var x = 1");
    testConversion("var x = 1 + 1");
    testConversion("var x = 'a' + 'b'");

    testConversion("var x, y, z");
    testConversion("var x = 2, y = 2 * x, z");

    testConversion("var x = function () {}");
    testConversion("var x = function f() {}");
    testConversion("var x = function f(arg1, arg2) {}");

    testConversion("var x = function f(arg1, arg2) {1}");
    testConversion("var x = function f(arg1, arg2) {x}");
    testConversion("var x = function f(arg1, arg2) {x = 2 * 3}");

    testConversion("var x = function f() {var x}");
    testConversion("var x = function f() {var y = (z + 2) * q}");

    testConversion("var x = function f(a, b) {"
        + "var y = function g(a, b) {z = a + b}}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testWhile
  public void testWhile() throws Exception {
     testConversion("while (true) {;}");
     testConversion("while (true) {f()}");
     testConversion("while (f(x, y)) {break;}");
     testConversion("while (f(f(f(x, y)))) {}");
     testConversion("while ((f(f(f(x, y))))()) {}");

     testConversion("while (x--) {2 + 3; q = 2 + 3; var v = y * z; "
         + "g = function(a) {true; var b = a + 1; return a * a}}");
   }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testWith
  public void testWith() throws Exception {
     testConversion("with ({}) {}");
     testConversion("with ({}) {;}");
     testConversion("with (x) {}");
     testConversion("with (x) {f(x)}");
     testConversion("with ({a: function f() {}}) {f(1)}");
     testConversion("with ({z: function f() {2 + 3; q = 2 + 3; var v = y * z;"
         + "g = function(a) {true; var b = a + 1; return a * a}}}) {f(1)}");
     testConversion("with (x in X) {x++}");
   }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }
