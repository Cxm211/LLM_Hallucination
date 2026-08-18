// com/google/javascript/jscomp/TypeCheckTest.java
public void testNewWithUnknownType() throws Exception {
  testTypes(
      "var f = function(x) {" +
      "  if (x) {" +
      "    new x();" +
      "  }" +
      "};");
}