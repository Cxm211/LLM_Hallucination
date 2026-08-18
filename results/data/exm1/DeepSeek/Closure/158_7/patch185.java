  public void testMethodInference6() throws Exception {
    testTypes(
        " function F() {}" +
        " F.prototype.foo = function(x) { };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function() { };" +
        "(new G()).foo(1);",
        "Function G.prototype.foo: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }