// com/google/javascript/jscomp/TypeCheckTest.java
public void testQualifiedNameInferenceNonGlobalRoot() throws Exception {
    testTypes(
        "function outer() { " +
        "  var obj = {}; " +
        "  (function() { " +
        "    /** @param {number} x */ obj.foo = function(x) {}; " +
        "  })(); " +
        "  obj.foo(true); " +
        "}",
        "actual parameter 1 of obj.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }
