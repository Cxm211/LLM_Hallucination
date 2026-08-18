// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testInterfaceWithThisType() {
    testSame(
        "/** @interface */ /** @this {A} */ function A() { this.x = 1; }");
  }