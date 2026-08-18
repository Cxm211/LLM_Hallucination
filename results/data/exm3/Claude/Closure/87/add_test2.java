// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testIssue291_additionalCase3() {
  fold("if (f) { f.regularMethod(); }", "f && f.regularMethod();");
  fold("if (f) { f.myFunction(); }", "f && f.myFunction();");
}