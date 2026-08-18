// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java
public void testGuessModuleNameWithSubdirectory() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(null, "foo/bar");
    assertEquals("module$baz", pass.guessCJSModuleName("foo/bar/baz.js"));
    assertEquals("module$baz", pass.guessCJSModuleName("foo/bar\\baz.js"));
    assertEquals("module$qux", pass.guessCJSModuleName("foo/bar\\qux.js"));
  }
