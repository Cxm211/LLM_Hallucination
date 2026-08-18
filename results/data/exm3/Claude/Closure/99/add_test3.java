// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testConstructorWithoutAnnotation() {
    testFailure("function A() { this.x = 1; }");
  }