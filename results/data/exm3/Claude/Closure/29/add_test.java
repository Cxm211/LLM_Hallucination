// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testObjectPropertyAccessNotInlinable() {
    testSameLocal(
        "var obj; obj = {a:1}; var x = obj.b.c;");
  }