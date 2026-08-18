// com/google/javascript/jscomp/TypeCheckTest.java
public void testNewWithEmptyType() throws Exception {
  testTypes(
      "/** @constructor */" +
      "var A = function() {};" +
      "/** @constructor */" +
      "var B = function() {};" +
      "var f = function() {" +
      "  var C = (A === B) ? A : null;" +
      "  if (C) {" +
      "    new C();" +
      "  }" +
      "};");
}