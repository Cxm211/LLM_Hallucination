  public void testSuperClassDefinedAfterSubClass1() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " function Base() {}" +
        " " +
        "function foo(x) { return x; }");
  }