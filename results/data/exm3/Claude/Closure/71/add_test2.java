// com/google/javascript/jscomp/CheckAccessControlsTest.java
public void testProtectedPropertyAccess() {
    // Test protected property access from non-subclass
    test(new String[] {
      "/** @constructor */ function Foo() {} " +
      "/** @protected */ Foo.prototype.bar_ = function() {};",
      "/** @constructor */ function Other() {}" +
      "Other.prototype.baz = function() { var f = new Foo(); f.bar_(); };"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }