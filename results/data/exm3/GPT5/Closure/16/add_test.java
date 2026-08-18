// com/google/javascript/jscomp/ScopedAliasesTest.java::testIssue772
testTypes(
        "var b = a.b;" +
        "var c = b.c;",
        "/** @param {c} x */ types.actual;" +
        "/** @param {a.b.c} x */ types.expected;");