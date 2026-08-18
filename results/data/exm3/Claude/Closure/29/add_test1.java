// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testObjectPropertyChainAccess() {
    testSameLocal(
        "var obj = {x:1}; obj.y.z();");
  }