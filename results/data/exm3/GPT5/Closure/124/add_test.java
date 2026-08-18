// com/google/javascript/jscomp/ExploitAssignsTest.java::testIssue1017
public void testIssue1017_deeperChain() {
  testSame("x = x.a.b.c; x = x.a.b.c;");
}