// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java::testGuessModuleName
ProcessCommonJSModules pass2 = new ProcessCommonJSModules(null, "foo/");
assertEquals("module$baz", pass2.guessCJSModuleName("foo\\baz.js"));