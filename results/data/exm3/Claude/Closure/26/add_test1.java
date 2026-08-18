// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java
public void testEmptyFilename() {
  setFilename("");
  test(
      "exports.foo = 1;",
      "goog.provide('module$');" +
      "var module$ = {};" +
      "module$.foo = 1;");
}