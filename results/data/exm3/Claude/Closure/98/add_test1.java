// com/google/javascript/jscomp/InlineVariablesTest.java
public void testNoInlineAliasesInWhileLoop() {
  testSame(
      "function f() { " +
      "  var i = 0;" +
      "  while (i < 5) {" +
      "    var x = extern();" +
      "    var y = x;" +
      "    extern(y);" +
      "    i++;" +
      "  }" +
      "}");
}