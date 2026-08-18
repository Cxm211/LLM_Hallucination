// com/google/javascript/jscomp/TypeCheckTest.java
public void testGetpropAssignmentWithUndefined() throws Exception {
  testTypes("var x = undefined; x.prop = 3;",
      "No properties on this expression\n" +
      "found   : undefined\n" +
      "required: Object");
}