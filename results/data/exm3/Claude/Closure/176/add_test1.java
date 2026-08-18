// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1056_functionType() throws Exception {
    testTypes(
        "/** @type {Function} */ var f = null;" +
        "f();",
        "No properties on this expression\n" +
        "found   : null\n" +
        "required: Object");
  }