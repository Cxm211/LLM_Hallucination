// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testIssue291_additionalCase1() {
  foldSame("if (f) { f.onclick(); }");
  foldSame("if (f) { f.onload(); }");
  foldSame("if (f) { f.onmouseover(); }");
}