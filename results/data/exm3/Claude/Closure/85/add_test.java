// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testRemoveBreakWhenFollowNodeIsNull() {
  testSame("function foo() { switch(a) { case 'x': break; } }");
}