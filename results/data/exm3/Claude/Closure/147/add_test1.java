// com/google/javascript/jscomp/RuntimeTypeCheckTest.java
public void testValueWithMultipleInnerFns() {
  testChecks("/** @param {number} i */ function f(i) { function g() {} function h() {} }",
      "function f(i) {" +
      "  function g() {}" +
      "  function h() {}" +
      "  jscomp.typecheck.checkType(i, " +
      "      [jscomp.typecheck.valueChecker('number')]);" +
      "}");
}