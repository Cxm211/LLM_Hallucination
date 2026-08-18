// com/google/javascript/jscomp/RuntimeTypeCheckTest.java::testValueWithInnerFn
public void testValueWithTwoInnerFns() {
    testChecks("/** @param {number} i */ function f(i) { function g() {} function h() {} }",
        "function f(i) {" +
        "  function g() {}" +
        "  function h() {}" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }