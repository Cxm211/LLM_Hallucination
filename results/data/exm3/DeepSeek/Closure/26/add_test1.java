// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testProcessCJSEntryModulePrefixNoSlash() {
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    setFilename(0, "foo/bar-baz.js");
    test("exports.test = 1",
        "var module$foo$bar_baz={test:1};");
  }
