// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testProcessCJSEntryModuleWithDash() {
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=test-test");
    setFilename(0, "test-test.js");
    test("exports.test = 1",
        "var module$test_test={test:1};");
  }
