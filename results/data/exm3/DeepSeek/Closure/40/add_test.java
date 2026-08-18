// com/google/javascript/jscomp/IntegrationTest.java
public void testIssue284_extra() {
    CompilerOptions options = createCompilerOptions();
    options.smartNameRemoval = true;
    test(options,
        "var goog = {};" +
        "goog.inherits = function(x, y) {};" +
        "var ns = {};" +
        "/** @constructor */" +
        "ns.PageSelectionModel = function() {};" +
        "/** @constructor */" +
        "ns.PageSelectionModel.FooEvent = function() {};" +
        "goog['inherits'](ns.PageSelectionModel.ChangeEvent," +
        "    ns.PageSelectionModel.FooEvent);",
        "");
  }
