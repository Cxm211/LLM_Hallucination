  public void testStubFunctionDeclaration8() throws Exception {
    testFunctionType(
        " var f = function() {}; ",
        "f",
        createOptionalType(createNullableType(U2U_CONSTRUCTOR_TYPE))
            .toString());
  }