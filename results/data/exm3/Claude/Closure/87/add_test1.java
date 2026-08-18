// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testIssue291_additionalCase2() {
  foldSame("if (f) { f.toString(); }");
  foldSame("if (f) { f.valueOf(); }");
}