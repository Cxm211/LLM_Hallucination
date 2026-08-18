// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java::testDoNotInlineCatchExpression2
public void testDoNotInlineCatchExpression2() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "}catch(err) {" +
        "   a = err ? 1 : 2;\n" +
        "}\n" +
        "return a.stack\n");
  }