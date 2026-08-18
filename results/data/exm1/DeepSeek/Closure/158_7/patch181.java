  public void testMethodInference2() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.F = function() {};" +
        " goog.F.prototype.foo = " +
        "    function() { return 3; };" +
        " " +
        "goog.G = function() {};" +
        " goog.G.prototype.foo = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }