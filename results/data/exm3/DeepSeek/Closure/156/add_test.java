// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testEnumSingleAssignment() {
    test(
        "var ns = {};" +
        "/** @enum {number} */" +
        "ns.MyEnum = {A: 1, B: 2};" +
        "alias(ns);",
        "var ns = {};" +
        "/** @enum {number} */" +
        "var ns$MyEnum = {A: 1, B: 2};" +
        "alias(ns);",
        null,
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }
