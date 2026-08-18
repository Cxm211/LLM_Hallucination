// com/google/javascript/jscomp/IntegrationTest.java
public void testIncompleteFunctionWithArgs() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "var foo = {bar: function(a, b, c) }" },
        new String[] { "var foo = {bar: function(a, b, c){}};"},
        warnings
    );
  }