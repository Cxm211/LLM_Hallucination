// com/google/javascript/jscomp/IntegrationTest.java::testIncompleteFunction
{
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "var foo = {bar: function f(e) }" },
        new String[] { "var foo = {bar: function f(e){}};" },
        warnings
    );
  }