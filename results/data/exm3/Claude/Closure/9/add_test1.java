// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java
public void testGuessModuleNameNoPrefix() {
  ProcessCommonJSModules pass = new ProcessCommonJSModules(null, "foo");
  assertEquals("module$bar$baz",
      pass.guessCJSModuleName("bar\\baz.js"));
}