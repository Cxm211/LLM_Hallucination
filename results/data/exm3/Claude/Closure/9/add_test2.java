// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java
public void testGuessModuleNameEmptyPrefix() {
  ProcessCommonJSModules pass = new ProcessCommonJSModules(null, "");
  assertEquals("module$foo$bar",
      pass.guessCJSModuleName("foo\\bar.js"));
}