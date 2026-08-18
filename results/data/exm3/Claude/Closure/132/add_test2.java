// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testIssue925_AdditionalCase3() {
    testSame(
        "if (cond) {\n" +
        "    x[sideEffect()] = 1;\n" +
        "} else {\n" +
        "    x[sideEffect()] = 2;\n" +
        "}");
  }