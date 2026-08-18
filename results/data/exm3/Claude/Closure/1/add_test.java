// com/google/javascript/jscomp/RemoveUnusedVarsTest.java
public void testRemoveUnusedParamAtEnd() {
    removeGlobal = true;
    test("function f(a, b, c) { use(a); }",
         "function f(a) { use(a); }");
  }