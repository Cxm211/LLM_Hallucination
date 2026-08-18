// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testDoNotInlineCatchExpressionInsideCatchBlock() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "}catch(err) {\n" +
        "   a = err;\n" +
        "   return a.stack;\n" +
        "}");
  }
