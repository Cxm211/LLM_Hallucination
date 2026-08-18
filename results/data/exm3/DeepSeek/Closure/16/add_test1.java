// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testAliasInUnionType() {
  testTypes(
      "var b = a.b;" +
      "var c = b.c;",
      "/** @param {c.MyType|string} x */ types.actual;" +
      "/** @param {a.b.c.MyType|string} x */ types.expected;");
}
