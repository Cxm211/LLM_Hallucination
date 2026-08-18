// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java
public void testFilenameWithoutPrefix() {
  setFilename("bar/baz");
  test(
      "exports.foo = 1;",
      "goog.provide('module$bar$baz');" +
      "var module$bar$baz = {};" +
      "module$bar$baz.foo = 1;");
}