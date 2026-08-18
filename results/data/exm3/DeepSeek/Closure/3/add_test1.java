// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testDoNotInlineCatchExpressionWithMultipleStatements() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "}catch(err) {\n" +
        "   a = err;\n" +
        "   var b = 1;\n" +
        "}\n" +
        "return a.stack;\n");
  }
