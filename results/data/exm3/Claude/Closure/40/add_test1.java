// com/google/javascript/jscomp/NameAnalyzerTest.java
public void testIssue284_MultipleInheritsUndefined() {
    test(
        "var goog = {};" +
        "goog.inherits = function(x, y) {};" +
        "var ns = {};" +
        "/** @constructor */" +
        "ns.A = function() {};" +
        "goog.inherits(ns.B, ns.UndefinedX);" +
        "goog.inherits(ns.C, ns.UndefinedY);",
        "");
  }