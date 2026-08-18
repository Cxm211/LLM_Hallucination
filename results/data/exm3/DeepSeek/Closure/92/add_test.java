// com/google/javascript/jscomp/ProcessClosurePrimitivesTest.java
public void testProvideInIndependentModules5() {
    test(
        createModuleStar(
            "goog.provide('a');",
            "goog.provide('a.b.c.d');",
            "goog.provide('a.b.c.e');"),
        new String[] {
            "var a = {};a.b = {};a.b.c = {}",
            "a.b.c.d = {};",
            "a.b.c.e = {};",
        });
  }
