// com/google/javascript/jscomp/ExploitAssignsTest.java
public void testIssue1017_SingleLevel() {
  testSame("x = x.parent; x = x.parent;");
}