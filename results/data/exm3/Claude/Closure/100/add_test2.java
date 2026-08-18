// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testNestedPropertyAccess() {
    testFailure("var a = this.foo.bar;");
  }