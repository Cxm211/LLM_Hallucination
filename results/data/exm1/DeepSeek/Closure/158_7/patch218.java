  public void testTypeRedefinition() throws Exception {
    testTypes("a={}; a.A = {ZOR:'b';}"
        + " a.A = function() {}",
        "variable a.A redefined with type function (new:a.A): undefined, " +
        "original definition at [testcode]:1 with type enum{a.A}");
  }