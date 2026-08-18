// com/google/javascript/jscomp/TypeCheckTest.java
public void testQualifiedNameInference6() throws Exception {
    testTypes(
        "var ns = {}; " +
        "(function() { " +
        "    (function() { " +
        "        /** @param {string} x */ ns.bar = function(x) {}; " +
        "    })(); " +
        "})();" +
        "ns.bar(123);",
        "actual parameter 1 of ns.bar does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }