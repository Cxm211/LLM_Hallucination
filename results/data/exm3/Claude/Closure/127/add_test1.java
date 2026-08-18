// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testRemoveContinueNotInFinally() throws Exception {
  test(
    "function f() {for(var i=0;i<10;i++) {if(x) continue;}}",
    "function f() {for(var i=0;i<10;i++) {if(x) {}}} "
  );
}