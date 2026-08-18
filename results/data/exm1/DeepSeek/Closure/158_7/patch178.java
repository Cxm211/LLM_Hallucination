  public void testAbstractMethodHandling5() throws Exception {
    testTypes(
        " var abstractFn = function() {};" +
        " var f = abstractFn;" +
        "f('x');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }