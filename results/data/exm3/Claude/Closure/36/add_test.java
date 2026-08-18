// com/google/javascript/jscomp/IntegrationTest.java
public void testSingletonGetterInlineFunction() {
  CompilerOptions options = createCompilerOptions();
  CompilationLevel.ADVANCED_OPTIMIZATIONS
      .setOptionsForCompilationLevel(options);
  options.setCodingConvention(new ClosureCodingConvention());
  test(options,
      "/** @const */\n" +
      "var goog = goog || {};\n" +
      "goog.addSingletonGetter = function(ctor) {\n" +
      "  ctor.getInstance = function() {\n" +
      "    return ctor.instance_ || (ctor.instance_ = new ctor());\n" +
      "  };\n" +
      "};" +
      "function Foo() {}\n" +
      "goog.addSingletonGetter(Foo);" +
      "var f = Foo.getInstance;" +
      "f();",
      "");
}