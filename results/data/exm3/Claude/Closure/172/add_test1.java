// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1024_AdditionalCase2() throws Exception {
     testTypes(
        "var obj = {a: 1};\n" +
        "obj.prototype = {b: 2};\n" +
        "/** @param {Object} x */\n" +
        "function h(x) {\n" +
        "  x.prototype = 'test';\n" +
        "}\n");
  }