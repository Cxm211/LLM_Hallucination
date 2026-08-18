// com/google/javascript/jscomp/TypeCheckTest.java
public void testQualifiedNameInferenceNested() throws Exception {
    testTypes(
        "var ns = {}; ns.sub = {}; " +
        "(function() { " +
        "    /** @param {number} x */ ns.sub.foo = function(x) {}; })();" +
        "(function() { ns.sub.foo(true); })();",
        "actual parameter 1 of ns.sub.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }
