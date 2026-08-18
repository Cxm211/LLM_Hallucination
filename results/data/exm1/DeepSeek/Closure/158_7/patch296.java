  public void testGoodExtends8() throws Exception {
    testTypes(" function Sub() {}" +
        " function f() { return (new Sub()).foo; }" +
        " function Base() {}" +
        " Base.prototype.foo = true;",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }