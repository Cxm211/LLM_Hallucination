// com/google/javascript/jscomp/ExploitAssignsTest.java
public void testIssue1017_NestedGetProp() {
  testSame("x = x.a.b.c; x = x.a.b.c;");
}