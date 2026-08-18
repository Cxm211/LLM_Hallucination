// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testGetMsgWiringAdvancedWithTranslationsFile() throws Exception {
  String translationsFile = "test_translations.xtb";
  args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
  args.add("--translations_file=" + translationsFile);
  test("/** @desc A foo. */ var MSG_FOO = goog.getMsg('foo');", "");
}