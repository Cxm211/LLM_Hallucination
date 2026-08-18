// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testRemoveContinueWhenFollowNodeIsNull() {
  testSame("function foo() { while(true) { switch(a) { case 'x': continue; } } }");
}