// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testIssue925_AdditionalCase1() {
    test(
        "if (x.foo()) {\n" +
        "    x.y = 0;\n" +
        "} else {\n" +
        "    x.y = 1;\n" +
        "}",
        "x.y = x.foo() ? 0 : 1;");
  }