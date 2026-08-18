// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testIssue772_nullQualifiedName() {
    testTypes(
        "var b = someFunction();",
        "/** @param {b.Type} x */ types.actual;" +
        "/** @param {b.Type} x */ types.expected;");
  }