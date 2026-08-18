// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testFunctionDeclaration() {
    test(
        "function alias() {}" +
        "var ns = {};" +
        "ns.fn = function() {};" +
        "alias(ns);",
        "function alias() {}" +
        "var ns = {};" +
        "var ns$fn = function() {};" +
        "alias(ns);",
        null,
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }
