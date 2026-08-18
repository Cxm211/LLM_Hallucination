  public void testStubFunctionDeclaration9() throws Exception {
    testFunctionType(
        " var f; ",
        "f",
        "function (): number");
  }