// com/google/javascript/jscomp/TypeCheckTest.java
public void testInterfaceInheritanceCheck13() throws Exception {
    testTypes(
        "/** @interface */ function I() {};\n" +
        "/** @type {boolean} */ I.prototype.foo;\n" +
        "/** \n * @constructor \n * @implements {I} */\n" +
        "function C() {\n" +
        "/** \n * @type {number} */ this.foo = 2;};\n" +
        "/** @type {I} */ \n var test = new C(); alert(test.foo);",
        "mismatch of the foo property type and the type of the property" +
        " it overrides from interface I\n" +
        "original: boolean\n" +
        "override: number");
  }
