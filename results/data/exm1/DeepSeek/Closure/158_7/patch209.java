  public void testStubFunctionDeclaration3() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.foo;",
        "f.foo",
        "function (): undefined");
  }