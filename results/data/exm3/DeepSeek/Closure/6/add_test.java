// com/google/javascript/jscomp/TypeCheckTest.java
public void testEnumConstructorVariableAssignment() throws Exception {
    testTypes(
        "/** @enum {string} */ var E = {A: 'a'};" +
        "/** @constructor */ function F() {};" +
        "/** @type {E} */ var x = F;",
        "initializing variable\n" +
        "found   : function (new:F): undefined\n" +
        "required: enum{E}");
  }
