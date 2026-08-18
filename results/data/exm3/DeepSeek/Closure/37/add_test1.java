// com/google/javascript/jscomp/IntegrationTest.java
public void testIncompleteFunctionDeclaration() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "function foo(e) }" },
        new String[] { "function foo(e){}" },
        warnings
    );
  }
