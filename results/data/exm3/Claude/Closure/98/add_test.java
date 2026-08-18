// com/google/javascript/jscomp/InlineVariablesTest.java
public void testNoInlineAliasesInNestedLoop() {
  testSame(
      "function f() { " +
      "  for (var i = 0; i < 5; i++) {" +
      "    for (var j = 0; j < 3; j++) {" +
      "      var x = extern();" +
      "      var y = x;" +
      "      extern(y);" +
      "    }" +
      "  }" +
      "}");
}