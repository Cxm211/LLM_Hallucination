// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testDontRemoveContinueInNestedFinally() throws Exception {
  testSame(
    "function f() {a: do {try {throw 9} finally {try {} finally {continue a}}} while(false); alert(1);}"
  );
}