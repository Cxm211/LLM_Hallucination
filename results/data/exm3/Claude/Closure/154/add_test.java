// com/google/javascript/jscomp/TypeCheckTest.java
public void testInterfaceInheritanceCheck12_AdditionalCase1() throws Exception {
    testTypes(
        "/** @interface */ function I() {};\n" +
        "/** @type {string} */ I.prototype.prop;\n" +
        "/** @interface \n * @extends {I} */ function J() {};\n" +
        "/** \n * @constructor \n * @implements {J} */\n" +
        "function C() {\n" +
        "/** \n * @type {number} */ this.prop = 5;};\n",
        "mismatch of the prop property type and the type of the property" +
        " it overrides from interface I\n" +
        "original: string\n" +
        "override: number");
  }