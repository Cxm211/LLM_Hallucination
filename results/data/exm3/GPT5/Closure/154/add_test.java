// com/google/javascript/jscomp/TypeCheckTest.java::testInterfaceInheritanceCheck12
public void testInterfaceInheritanceCheck12b() throws Exception {
    testTypes(
        "/** @interface */ function I() {};\n" +
        "/** @type {number} */ I.prototype.foobar;\n" +
        "/** \n * @constructor \n * @implements {I} */\n" +
        "function C() {\n" +
        "/** \n * @type {string} */ this.foobar = 'x';};\n" +
        "/** @type {I} */ \n var test = new C(); alert(test.foobar);",
        "mismatch of the foobar property type and the type of the property it overrides from interface I\noriginal: number\noverride: string");
  }