  public void testFunctionInference16() throws Exception {
    testFunctionType(
        " function f() {};" +
        "f.prototype.foo = function(){};",
        "(new f).foo",
        "function (this:f): undefined");
  }