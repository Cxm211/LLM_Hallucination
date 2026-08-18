// buggy function
  private String normalizeSourceName(String filename) {
    if (filename.indexOf(filenamePrefix) == 0) {
      filename = filename.substring(filenamePrefix.length());
    }
    return filename;
  }

    private void emitOptionalModuleExportsOverride(Node script,
        String moduleName) {

      Node moduleExportsProp = IR.getprop(IR.name(moduleName),
          IR.string("module$exports"));
      script.addChildToBack(IR.ifNode(
          moduleExportsProp,
          IR.block(IR.exprResult(IR.assign(IR.name(moduleName),
              moduleExportsProp.cloneTree())))).copyInformationFromForTree(
          script));
    }

    private void visitModuleExports(Node prop) {
      String moduleName = guessCJSModuleName(prop.getSourceFileName());
      Node module = prop.getChildAtIndex(0);
      module.putProp(Node.ORIGINALNAME_PROP, "module");
      module.setString(moduleName);
      Node exports = prop.getChildAtIndex(1);
      exports.putProp(Node.ORIGINALNAME_PROP, "exports");
      exports.setString("module$exports");
    }

// trigger testcase
// com/google/javascript/jscomp/CommandLineRunnerTest.java::testProcessCJS
public void testProcessCJS() {
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    setFilename(0, "foo/bar.js");
    test("exports.test = 1",
        "var module$foo$bar={test:1};");
  }

// com/google/javascript/jscomp/CommandLineRunnerTest.java::testTransformAMDAndProcessCJS
public void testTransformAMDAndProcessCJS() {
    args.add("--transform_amd_modules");
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    setFilename(0, "foo/bar.js");
    test("define({foo: 1})",
        "var module$foo$bar={}, module$foo$bar={foo:1};");
  }

// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java::testDash
public void testDash() {
    setFilename("test-test");
    test(
        "var name = require('name'); exports.foo = 1;",
        "goog.provide('module$test_test');" +
        "var module$test_test = {};" +
        "goog.require('module$name');" +
        "var name$$module$test_test = module$name;" +
        "module$test_test.foo = 1;");
  }

// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java::testExports
public void testExports() {
    setFilename("test");
    test(
        "var name = require('name');" +
        "exports.foo = 1;",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "goog.require('module$name');" +
        "var name$$module$test = module$name;" +
        "module$test.foo = 1;");
    test(
        "var name = require('name');" +
        "module.exports = function() {};",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "goog.require('module$name');" +
        "var name$$module$test = module$name;" +
        "module$test.module$exports = function() {};" +
        "if(module$test.module$exports)" +
        "module$test=module$test.module$exports");
  }

// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java::testModuleName
public void testModuleName() {
    assertEquals("module$foo$baz",
        ProcessCommonJSModules.toModuleName("./baz.js", "foo/bar.js"));
    assertEquals("module$foo$baz_bar",
        ProcessCommonJSModules.toModuleName("./baz-bar.js", "foo/bar.js"));
    assertEquals("module$baz",
        ProcessCommonJSModules.toModuleName("../baz.js", "foo/bar.js"));
    assertEquals("module$baz",
        ProcessCommonJSModules.toModuleName("../../baz.js", "foo/bar/abc.js"));
    assertEquals("module$baz", ProcessCommonJSModules.toModuleName(
        "../../../baz.js", "foo/bar/abc/xyz.js"));
    setFilename("foo/bar");
    test(
        "var name = require('name');",
        "goog.provide('module$foo$bar'); var module$foo$bar = {};" +
        "goog.require('module$name');" +
        "var name$$module$foo$bar = module$name;");
    test(
        "var name = require('./name');",
        "goog.provide('module$foo$bar');" +
        "var module$foo$bar = {};" +
        "goog.require('module$foo$name');" +
        "var name$$module$foo$bar = module$foo$name;");

  }

// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java::testVarRenaming
public void testVarRenaming() {
    setFilename("test");
    test(
        "var a = 1, b = 2;" +
        "(function() { var a; b = 4})()",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "var a$$module$test = 1, b$$module$test = 2;" +
        "(function() { var a; b$$module$test = 4})();");
  }

// com/google/javascript/jscomp/ProcessCommonJSModulesTest.java::testWithoutExports
public void testWithoutExports() {
    setFilename("test");
    test(
        "var name = require('name');" +
        "name()",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "goog.require('module$name');" +
        "var name$$module$test = module$name;" +
        "name$$module$test();");
    setFilename("test/sub");
    test(
        "var name = require('mod/name');" +
        "(function() { name(); })();",
        "goog.provide('module$test$sub');" +
        "var module$test$sub = {};" +
        "goog.require('module$mod$name');" +
        "var name$$module$test$sub = module$mod$name;" +
        "(function() { name$$module$test$sub(); })();");
  }
