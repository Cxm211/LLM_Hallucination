// com/google/javascript/jscomp/TypeCheckTest.java::testIssue1056
testTypes(
        "/** @type {Object} */ var y = null; y.foo;",
        "No properties on this expression\n" +
        "found   : null\n" +
        "required: Object");