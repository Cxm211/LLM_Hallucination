// com/google/javascript/jscomp/CheckAccessControlsTest.java
public void testPrivateOverrideWithProtected() {
    test(new String[] {
      "/** @constructor */ function Foo() {} " +
      "/** @private */ Foo.prototype.bar = function() {};",
      "/** @constructor \n * @extends {Foo} */ " +
      "function SubFoo() {};" +
      "/** @protected */ SubFoo.prototype.bar = function() {};"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }
