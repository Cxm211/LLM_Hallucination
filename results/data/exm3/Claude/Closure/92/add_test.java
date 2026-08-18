// com/google/javascript/jscomp/ProcessClosurePrimitivesTest.java
public void testProvideInIndependentModules5() {
    // Test even deeper nesting: 5 levels
    test(
        createModuleStar(
            "goog.provide('a');",
            "goog.provide('a.b.c.d.E');",
            "goog.provide('a.b.c.d.F');"),
        new String[] {
            "var a = {};a.b = {};a.b.c = {};a.b.c.d = {}",
            "a.b.c.d.E = {};",
            "a.b.c.d.F = {};",
        });
  }