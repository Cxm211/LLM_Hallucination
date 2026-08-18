// com/google/javascript/jscomp/CheckAccessControlsTest.java
public void testNoProtectedAccessFromNonSubclass() {
    test(new String[] {
      "/** @constructor */ function Foo() {} " +
      "/** @protected */ Foo.prototype.bar = function() {};",
      "/** @constructor */ function Other() {} " +
      "Other.prototype.test = function() { (new Foo()).bar(); };"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }
