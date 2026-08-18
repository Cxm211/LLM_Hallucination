  public void testMethodInference3() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(x) { return x; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }