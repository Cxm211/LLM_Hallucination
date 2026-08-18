  public void testBackwardsConstructor2() throws Exception {
    testTypes(
        "function f() { (new Foo(true)); }" +
        "" +
        "var YourFoo = function(x) {};" +
        "" +
        "var Foo = YourFoo;",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }