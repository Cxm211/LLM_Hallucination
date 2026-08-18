// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testIssue772_deeperNesting() {
    testTypes(
        "var b = a.b;" +
        "var c = b.c;" +
        "var d = c.d;",
        "/** @param {d.MyType} x */ types.actual;" +
        "/** @param {a.b.c.d.MyType} x */ types.expected;");
  }