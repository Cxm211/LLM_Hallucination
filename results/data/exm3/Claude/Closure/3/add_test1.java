// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testDoNotInlineCatchExpression4() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "} catch(err) {" +
        "  a = err || 'default';\n" +
        "}\n" +
        "return a\n");
  }