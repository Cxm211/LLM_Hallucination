// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1002_variant2() throws Exception {
    testTypes(
        "/** @constructor */" +
        "var D1 = function() {};" +
        "/** @constructor */" +
        "var D2 = function() {};" +
        "var f = function() {" +
        "  if (D1 === D2) {" +
        "    new D1();" +
        "  }" +
        "};");
  }
