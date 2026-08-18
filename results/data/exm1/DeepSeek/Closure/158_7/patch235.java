  public void testEnumStaticMethod1() throws Exception {
    testTypes(
        " var Foo = {AAA: 1};" +
        " Foo.method = function(x) {};" +
        "Foo.method(true);",
        "actual parameter 1 of Foo.method does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }