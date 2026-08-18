// com/google/javascript/jscomp/ExploitAssignsTest.java
public void testNestedPropertyDepth3() {
  testSame("x = x.a.b.c; x = x.a.b.c;");
}
