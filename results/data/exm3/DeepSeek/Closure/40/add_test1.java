// com/google/javascript/jscomp/NameAnalyzerTest.java
public void testIssue284_extra2() {
    test(
        "var goog = {};" +
        "goog.inherits = function(x, y) {};" +
        "var a = {};" +
        "a.b = {};" +
        "a.b.c = {};" +
        "/** @constructor */" +
        "a.b.c.Constructor = function() {};" +
        "goog.inherits(a.b.c.Constructor, Object);",
        "");
  }
