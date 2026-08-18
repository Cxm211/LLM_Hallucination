// com/google/javascript/jscomp/CheckAccessControlsTest.java
public void testNoPrivateAccessForProperties8_AdditionalBranch() {
    // Test instance property override in same file (should pass)
    testSame(
      "/** @constructor */ function Foo() { /** @private */ this.bar_ = 3; }" +
      "/** @constructor \n * @extends {Foo} */ " +
      "function SubFoo() { /** @private */ this.bar_ = 3; };"
    );
  }