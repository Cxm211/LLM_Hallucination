// com/google/javascript/jscomp/InlineVariablesTest.java
public void testNoInlineAliasesInDoWhileLoop() {
    testSame(
        "function f() { " +
        "  var i = 0; do {" +
        "    var x = extern();" +
        "    (function() {" +
        "       var y = x; window.setTimeout(function() { extern(y); }, 0);" +
        "     })();" +
        "    i++; } while (i < 5);" +
        "}");
  }
