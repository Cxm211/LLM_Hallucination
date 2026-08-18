// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testRemoveBreakNotInFinally() throws Exception {
  test(
    "function f() {a: {if (x) { break a; }} return 1;}",
    "function f() {a: {if (x) {}} return 1;}"
  );
}