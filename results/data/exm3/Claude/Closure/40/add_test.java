// com/google/javascript/jscomp/IntegrationTest.java
public void testIssue284_UndefinedInheritsParent() {
    CompilerOptions options = createCompilerOptions();
    options.smartNameRemoval = true;
    test(options,
        "var goog = {};" +
        "goog.inherits = function(x, y) {};" +
        "var ns = {};" +
        "/** @constructor */" +
        "ns.Child = function() {};" +
        "goog.inherits(ns.Child, ns.UndefinedParent);",
        "");
  }