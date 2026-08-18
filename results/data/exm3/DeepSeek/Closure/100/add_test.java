// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testGlobalThisPropertyRead() {
    testFailure("this.bar;");
  }
