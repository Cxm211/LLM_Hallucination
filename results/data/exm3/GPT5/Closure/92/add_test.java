// com/google/javascript/jscomp/ProcessClosurePrimitivesTest.java::testProvideInIndependentModulesDeep
public void testProvideInIndependentModulesDeep() {
    test(
        createModuleStar(
            "goog.provide('a.b');",
            "goog.provide('a.b.c.D');"),
        new String[] {
            "var a = {};a.b = {};a.b.c = {}",
            "a.b.c.D = {};",
        });
  }