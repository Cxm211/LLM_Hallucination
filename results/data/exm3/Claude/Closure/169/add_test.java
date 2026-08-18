// com/google/javascript/jscomp/TypeCheckTest.java
public void testFunctionTypeEquivalenceWithUnknownReturn() throws Exception {
    testTypes(
        "/** @param {{func: function(): number}} obj */" +
        "function test1(obj) {}" +
        "/** @type {{func: function(): ?}} */" +
        "var fnStruc1 = {func: function() { return 1; }};" +
        "test1(fnStruc1);");
  }