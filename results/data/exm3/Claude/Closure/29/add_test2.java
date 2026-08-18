// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testObjectPropertyAccessInVarDecl() {
    testSameLocal(
        "var obj; obj = {}; var result = obj.toString;");
  }