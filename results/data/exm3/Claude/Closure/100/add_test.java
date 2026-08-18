// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testGlobalThisWithoutPropertyAccess() {
    testSame("var a = this;");
  }