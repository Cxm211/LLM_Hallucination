// com/google/javascript/jscomp/IntegrationTest.java
public void testIssue772_withoutDot() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.checkTypes = true;
    test(
        options,
        "/** @const */ var a = {};" +
        "/** @const */ a.b = {};" +
        "goog.scope(function() {" +
        "  var c = a.b;" +
        "  /** @typedef {string} */" +
        "  c.MyType;" +
        "  /** @param {c} x The variable. */" +
        "  c.myFunc = function(x) {};" +
        "});",
        "/** @const */ var a = {};" +
        "/** @const */ a.b = {};" +
        "a.b.MyType;" +
        "a.b.myFunc = function(x) {};");
  }