// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java
public void testGuessModuleNameMixedSlashes() {
  ProcessCommonJSModules pass = new ProcessCommonJSModules(null, "foo");
  assertEquals("module$bar$baz",
      pass.guessCJSModuleName("foo/bar\\baz.js"));
  assertEquals("module$bar$baz",
      pass.guessCJSModuleName("foo\\bar/baz.js"));
}