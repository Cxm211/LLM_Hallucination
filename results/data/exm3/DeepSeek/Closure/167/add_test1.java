// com/google/javascript/jscomp/TypeCheckTest.java
public void testShortCircuitNoType() throws Exception {
    // If left side of && is impossible (NO_TYPE), right side should not be evaluated,
    // but property check on right side should still warn if property missing.
    testTypes(
        "/** @constructor */\n" +
        "var Foo = function() {};\n" +
        "Foo.prototype.bar = function() {};\n" +
        "/** @param {?Foo} x */\n" +
        "function f(x) {\n" +
        "  if (x && x.baz) { }\n" +
        "}",
        "Property baz never defined on Foo");
  }
