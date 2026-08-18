// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testRemoveReturnWhenFollowNodeIsNull() {
  testSame("function foo() { switch(a) { case 'x': return; } }");
}