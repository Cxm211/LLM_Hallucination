// com/google/javascript/jscomp/LooseTypeCheckTest.java
public void testPropertyConstructorEnumMismatch() throws Exception {
    testClosureTypesMultipleWarnings(
        "a={}; /** @constructor */ a.A = function() {};" +
        "/** @enum {string} */ a.B = {X: 'x'};" +
        "a.A = a.B;",
        Lists.newArrayList(
            "assignment to property A of a\n" +
            "found   : enum{a.B}\n" +
            "required: function (new:a.A): undefined"));
  }
