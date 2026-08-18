// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1056_undefined() throws Exception {
    testTypes(
        "/** @type {Object} */ var x = undefined;" +
        "x.foo = 'hi';",
        "No properties on this expression\n" +
        "found   : undefined\n" +
        "required: Object");
  }