// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1002_variant1() throws Exception {
    testTypes(
        "/** @interface */" +
        "var I1 = function() {};" +
        "/** @constructor @implements {I1} */" +
        "var C1 = function() {};" +
        "/** @constructor @implements {I1} */" +
        "var C2 = function() {};" +
        "var f = function() {" +
        "  if (C1 === C2) {" +
        "    new C1();" +
        "  }" +
        "};");
  }
