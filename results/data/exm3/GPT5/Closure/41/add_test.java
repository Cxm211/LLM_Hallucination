// com/google/javascript/jscomp/TypeCheckTest.java::testIssue368
testTypes(
        "/** @param {number} x @param {string} y */ function f(x, y) {}" +
        "/** @param {number} x */ function f(x) {}" +
        "f(1, 2);",
        "actual parameter 2 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");