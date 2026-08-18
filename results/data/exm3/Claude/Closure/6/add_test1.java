// com/google/javascript/jscomp/LooseTypeCheckTest.java
public void testConstructorToConstructorPropertyAssignment() throws Exception {
    testTypes(
        "var obj = {};" +
        "/** @constructor */ function F() {}" +
        "/** @constructor */ function G() {}" +
        "/** @type {function(new:F)} */ obj.prop = function() {};" +
        "obj.prop = G;",
        "assignment to property prop of obj\n" +
        "found   : function (new:G): undefined\n" +
        "required: function (new:F): ?");
  }