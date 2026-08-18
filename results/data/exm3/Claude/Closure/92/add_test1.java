// com/google/javascript/jscomp/ProcessClosurePrimitivesTest.java
public void testProvideInIndependentModules6() {
    // Test with 2-level nesting (simpler case)
    test(
        createModuleStar(
            "goog.provide('lib');",
            "goog.provide('lib.util.X');",
            "goog.provide('lib.util.Y');"),
        new String[] {
            "var lib = {};lib.util = {}",
            "lib.util.X = {};",
            "lib.util.Y = {};",
        });
  }