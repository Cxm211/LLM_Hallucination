// com/google/javascript/jscomp/TypeCheckTest.java
public void testObjectLiteralPropertyReassignmentWithType() throws Exception {
    testTypes(
        "/** @constructor */" +
        "function F() {}" +
        "F.prototype = { bar: function (x) { } };" + // no JSDoc, bar type inferred as ?
        "F.prototype = { /** @param {string} x */ bar: function (x) { } };" + // with JSDoc, should update type
        "(new F()).bar(true)", // should report type error
        "actual parameter 1 of F.prototype.bar does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string");
  }
