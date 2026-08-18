  public void testNestedFunctionInference1() throws Exception {
    String nestedAssignOfFooAndBar =
        " function f() {};" +
        "f.prototype.foo = f.prototype.bar = function(){};";
    testFunctionType(nestedAssignOfFooAndBar, "(new f).bar",
        "function (this:f): undefined");
  }