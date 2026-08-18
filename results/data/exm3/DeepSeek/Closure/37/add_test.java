// com/google/javascript/jscomp/IntegrationTest.java
public void testIncompleteFunctionGlobal() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "var bar = function(a,b) }" },
        new String[] { "var bar = function(a,b){};" },
        warnings
    );
  }
