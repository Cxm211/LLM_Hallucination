// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testNoInlineDeletedPropertiesMultiple() {
    testSameLocal(
        "var foo = {bar:1, baz:2};" +
        "delete foo.baz;" +
        "return foo.bar + foo.baz;");
  }