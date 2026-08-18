// com/google/javascript/jscomp/CheckAccessControlsTest.java
public void testNoPrivateAccessForProperties6_AdditionalBranch() {
    // Test overriding with explicit public visibility
    test(new String[] {
      "/** @constructor */ function Foo() {} " +
      "/** @private */ Foo.prototype.bar_ = function() {};",
      "/** @constructor \n * @extends {Foo} */ " +
      "function SubFoo() {};" +
      "/** @public */ SubFoo.prototype.bar_ = function() {};"
    }, null, VISIBILITY_MISMATCH);
  }