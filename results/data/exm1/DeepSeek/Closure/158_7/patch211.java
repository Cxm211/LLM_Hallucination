  public void testStubFunctionDeclaration5() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        createOptionalType(createNullableType(U2U_CONSTRUCTOR_TYPE))
            .toString());
  }