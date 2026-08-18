// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testIssue291_onclick() {
    foldSame("if (f) { f.onclick(); }");
  }
