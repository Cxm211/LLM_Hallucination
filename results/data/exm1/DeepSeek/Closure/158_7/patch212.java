  public void testStubFunctionDeclaration6() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo;",
        "(new f).foo",
        createOptionalType(createNullableType(U2U_CONSTRUCTOR_TYPE))
            .toString());
  }