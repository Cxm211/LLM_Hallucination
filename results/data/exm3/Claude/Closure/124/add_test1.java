// com/google/javascript/jscomp/ExploitAssignsTest.java
public void testIssue1017_DeeplyNested() {
  testSame("x = x.a.b.c.d.e; x = x.a.b.c.d.e;");
}