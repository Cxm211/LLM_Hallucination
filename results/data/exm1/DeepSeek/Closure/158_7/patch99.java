  public void testNumericComparison6() throws Exception {
    testTypes(" function foo() { if (3 >= foo()) return; }",
        "right side of numeric comparison\n" +
        "found   : undefined\n" +
        "required: number");
  }