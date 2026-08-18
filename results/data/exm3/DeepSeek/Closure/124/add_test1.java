// com/google/javascript/jscomp/ExploitAssignsTest.java
public void testDifferentPropertyChain() {
  testSame("x = x.a; x = x.a.b;");
}
