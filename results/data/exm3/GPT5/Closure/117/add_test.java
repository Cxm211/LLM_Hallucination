// com/google/javascript/jscomp/TypeCheckTest.java::testIssue1047
public void testIssue1047_additional() throws Exception {
    testTypes(
        "/** @interface */ function I() {}\n" +
        "/** @constructor @implements {I} */ function C() {}\n" +
        "/** @param {I} i */ function f(i) { var x = i.prop; }",
        "Property prop never defined on I");
  }