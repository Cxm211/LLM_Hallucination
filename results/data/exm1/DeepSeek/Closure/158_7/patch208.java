  public void testStubFunctionDeclaration2() throws Exception {
    testExternFunctionType(
        
        " function f() {};" +
        " f.subclass;",
        "f.subclass",
        "function (new:f.subclass): ?");
  }