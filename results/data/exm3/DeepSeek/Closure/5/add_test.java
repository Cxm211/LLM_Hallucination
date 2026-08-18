// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testNoInlineDeletedPropertyWithReassign() {
    testSameLocal(
        "var foo = {bar:1};" +
        "delete foo.bar;" +
        "foo.bar = 2;" +
        "return foo.bar;");
  }
