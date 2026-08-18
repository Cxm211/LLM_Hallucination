// com/google/javascript/jscomp/TypeCheckTest.java
public void testNewWithNoInstanceType() throws Exception {
  testTypes(
      "/** @constructor */" +
      "var A = function() {};" +
      "var f = function() {" +
      "  if (A === null) {" +
      "    new A();" +
      "  }" +
      "};");
}