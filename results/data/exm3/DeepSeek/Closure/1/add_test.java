// com/google/javascript/jscomp/RemoveUnusedVarsTest.java
public void testRemoveUnusedFunctionArgsDestructuring() {
    removeGlobal = false;
    test("function foo({x}) { }", 
         "function foo({x}) { }");
  }
