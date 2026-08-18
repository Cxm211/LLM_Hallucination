// com/google/javascript/jscomp/TypeCheckTest.java
public void testThisTypeOfFunction5() throws Exception {
    testTypes(
        "/** @interface */ function I() {}" +
        "/** @type {function(this:I)} */ function f() {}" +
        "f();",
        "\"function (this:I): ?\" must be called with a \"this\" type");
  }
