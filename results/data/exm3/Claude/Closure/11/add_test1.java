// com/google/javascript/jscomp/TypeCheckTest.java
public void testGetpropAssignmentWithValidObject() throws Exception {
  testTypes("var x = {}; x.prop = 3;");
}