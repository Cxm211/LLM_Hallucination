// com/google/javascript/jscomp/InlineObjectLiteralsTest.java::testNoInlineDeletedProperties_afterReassign
public void testNoInlineDeletedProperties_afterReassign() {
    testSameLocal(
        "var foo = {bar:1};" +
        "foo = {bar:2};" +
        "delete foo.bar;" +
        "return foo.bar;");
  }