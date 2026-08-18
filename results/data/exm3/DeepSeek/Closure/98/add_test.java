// com/google/javascript/jscomp/InlineVariablesTest.java
public void testNoInlineAliasesInWhileLoop() {
    testSame(
        "function f() { " +
        "  var i = 0; while (i < 5) {" +
        "    var x = extern();" +
        "    (function() {" +
        "       var y = x; window.setTimeout(function() { extern(y); }, 0);" +
        "     })();" +
        "    i++; }" +
        "}");
  }
