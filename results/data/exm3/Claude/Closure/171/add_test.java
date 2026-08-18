// com/google/javascript/jscomp/TypeCheckTest.java
public void testObjectLiteralPropertyTypeInference() throws Exception {
    testTypes(
        "/** @constructor */" +
        "function C() {}" +
        "C.prototype = {" +
        "  /** @param {number} x */" +
        "  foo: function(x) {}" +
        "};" +
        "(new C()).foo('string');",
        "actual parameter 1 of C.prototype.foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }