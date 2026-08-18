// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testNoInlineNestedDelete() {
    testSameLocal(
        "var foo = {bar: {baz: 1}};" +
        "delete foo.bar.baz;" +
        "return foo.bar.baz;");
  }
