// com/google/javascript/jscomp/IntegrationTest.java
public void testSingletonGetterExpression() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
    options.setCodingConvention(new ClosureCodingConvention());
    test(options,
        "/** @const */\n" +
        "var goog = goog || {};\n" +
        "goog.addSingletonGetter = function(ctor) {\n" +
        "  ctor.getInstance = function() {\n" +
        "    return ctor.instance_ || (ctor.instance_ = new ctor());\n" +
        "  };\n" +
        "};\n" +
        "function Foo() {}\n" +
        "goog.addSingletonGetter(Foo);\n" +
        "var result = Foo.getInstance() && Foo.getInstance().bar;\n",
        "");
  }
