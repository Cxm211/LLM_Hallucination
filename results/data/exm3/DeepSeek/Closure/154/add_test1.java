// com/google/javascript/jscomp/TypeCheckTest.java
public void testInterfaceInheritanceCheck14() throws Exception {
    testTypes(
        "/** @interface */ function I() {};\n" +
        "/** @type {function(number): string} */ I.prototype.func;\n" +
        "/** \n * @constructor \n * @implements {I} */\n" +
        "function C() {\n" +
        "/** \n * @type {function(string): string} */ this.func = function(x) { return ''; };};\n" +
        "/** @type {I} */ \n var test = new C(); alert(test.func);",
        "mismatch of the func property type and the type of the property" +
        " it overrides from interface I\n" +
        "original: function(number): string\n" +
        "override: function(string): string");
  }
