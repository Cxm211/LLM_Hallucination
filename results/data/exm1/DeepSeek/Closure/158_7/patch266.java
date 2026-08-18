  public void testEnum30() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A.B; }");
  }