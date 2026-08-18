// com/google/javascript/jscomp/RemoveUnusedVarsTest.java
public void testKeepUnusedParamInMiddle() {
    removeGlobal = true;
    test("function f(a, b, c) { use(a); use(c); }",
         "function f(a, b, c) { use(a); use(c); }");
  }