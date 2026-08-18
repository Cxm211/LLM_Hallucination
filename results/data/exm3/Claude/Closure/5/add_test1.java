// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testNoInlineDeletedPropertiesBeforeUse() {
    testSameLocal(
        "var foo = {bar:1};" +
        "var x = foo.bar;" +
        "delete foo.bar;" +
        "return x;");
  }