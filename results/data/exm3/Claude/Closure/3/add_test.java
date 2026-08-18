// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testDoNotInlineCatchExpression2() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "}catch(err) {" +
        "   a = [err];\n" +
        "}\n" +
        "return a[0]\n");
  }