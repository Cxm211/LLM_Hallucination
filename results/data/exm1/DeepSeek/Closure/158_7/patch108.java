  public void testValueOfComparison3() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.toString = function() { return 'o'; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }