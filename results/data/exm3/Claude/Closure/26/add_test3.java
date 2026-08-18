// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testProcessCJSWithModuleExports() {
  args.add("--process_common_js_modules");
  args.add("--common_js_entry_module=foo/bar");
  setFilename(0, "foo/bar.js");
  test("module.exports = {test: 1}",
      "var module$foo$bar={};module$foo$bar.module$exports={test:1};if(module$foo$bar.module$exports)module$foo$bar=module$foo$bar.module$exports");
}