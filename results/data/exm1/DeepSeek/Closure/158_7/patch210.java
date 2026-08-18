  public void testStubFunctionDeclaration4() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        "function (this:f): number");
  }