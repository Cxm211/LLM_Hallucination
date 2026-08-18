// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testIssue925_AdditionalCase2() {
    test(
        "if (a + b) {\n" +
        "    obj[key] = 1;\n" +
        "} else {\n" +
        "    obj[key] = 2;\n" +
        "}",
        "obj[key] = (a + b) ? 1 : 2;");
  }