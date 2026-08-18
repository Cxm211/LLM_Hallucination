// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testNestedAliasChain() {
  testTypes(
      "var a = global.a;" +
      "var b = a.b;" +
      "var c = b.c;",
      "/** @param {c.MyType} x */ types.actual;" +
      "/** @param {global.a.b.c.MyType} x */ types.expected;");
}
