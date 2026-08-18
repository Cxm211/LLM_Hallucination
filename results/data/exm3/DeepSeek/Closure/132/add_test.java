// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testAdditionalCases() {
    test(
        "if (x) { x -= 1; } else { x -= 2; }",
        "x ? x -= 1 : x -= 2");
    test(
        "if (c) { a.b = 1; } else { a.b = 2; }",
        "c ? a.b = 1 : a.b = 2");
    test(
        "if (c) { a[i] = 1; } else { a[i] = 2; }",
        "c ? a[i] = 1 : a[i] = 2");
  }
