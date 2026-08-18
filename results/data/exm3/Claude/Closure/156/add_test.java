// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testFunctionAssignmentWithoutEnum() {
    test(
        "function alias() {}" +
        "var ns = {};" +
        "ns.Class = function() {};" +
        "ns.Class = function() { return 1; };" +
        "alias(ns);",
        "function alias() {}" +
        "var ns = {};" +
        "var ns$Class = function() {};" +
        "ns$Class = function() { return 1; };" +
        "alias(ns);",
        null,
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }