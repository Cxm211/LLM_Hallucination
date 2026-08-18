// com/google/javascript/jscomp/TypeCheckTest.java
public void testAssignmentToTypedVariable() throws Exception {
    testTypes(
        "/** @type {number} */ var x = 5;" +
        "x = 'string';",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }
