// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testIssue291_additionalCase4() {
  fold("if (true) { regularCall(); }", "if (1) regularCall();");
  fold("if (f) { nonPropertyCall(); }", "f && nonPropertyCall();");
}