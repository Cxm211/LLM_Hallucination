// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testIssue291_computedEvent() {
    foldSame("if (f) { f['onclick'](); }");
  }
