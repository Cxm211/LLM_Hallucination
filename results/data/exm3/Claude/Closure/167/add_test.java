// com/google/javascript/jscomp/TypeCheckTest.java
public void testMissingProperty21() throws Exception {
    testTypes(
        "/** @param {Object} x */" +
        "function f(x) { if (!x.foo) { x.foo(); } }",
        "Property foo never defined on Object");
  }