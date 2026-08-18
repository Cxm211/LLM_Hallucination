// com/google/javascript/jscomp/TypeCheckTest.java
public void testInterfaceInheritanceCheck12_AdditionalCase2() throws Exception {
    testTypes(
        "/** @interface */ function I1() {};\n" +
        "/** @type {string} */ I1.prototype.method;\n" +
        "/** @interface */ function I2() {};\n" +
        "/** @type {string} */ I2.prototype.method;\n" +
        "/** \n * @constructor \n * @implements {I1} \n * @implements {I2} */\n" +
        "function C() {\n" +
        "/** \n * @type {boolean} */ this.method = true;};\n",
        new String[] {
            "mismatch of the method property type and the type of the property" +
            " it overrides from interface I1\n" +
            "original: string\n" +
            "override: boolean",
            "mismatch of the method property type and the type of the property" +
            " it overrides from interface I2\n" +
            "original: string\n" +
            "override: boolean"
        });
  }