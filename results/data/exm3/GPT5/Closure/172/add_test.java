// com/google/javascript/jscomp/TypeCheckTest.java::testIssue1024
testTypes(
        "/** @param {Object} a */\n" +
        "function h(a) {\n" +
        "  a.b = {};\n" +
        "  a.b.prototype = 1;\n" +
        "}\n");