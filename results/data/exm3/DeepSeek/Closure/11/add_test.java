// com/google/javascript/jscomp/TypeCheckTest.java
public void testLeftSideNullWithType() throws Exception {
    testTypes(
        "/** @type {null} */ var x = null;" +
        "x.prop = 3;",
        "No properties on this expression\n" +
        "found   : null\n" +
        "required: Object");
  }
