// com/google/javascript/jscomp/RuntimeTypeCheckTest.java
public void testValueWithMultipleInnerFns() {
    testChecks("/** @param {string} s */ function f(s) { function g() {} function h() {} }",
        "function f(s) {" +
        "  function g() {}" +
        "  function h() {}" +
        "  jscomp.typecheck.checkType(s, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }
