// com/google/javascript/jscomp/TypeCheckTest.java
public void testEnumToEnumAssignment() throws Exception {
    testTypes(
        "/** @enum {number} */ var E1 = {A: 1};" +
        "/** @enum {number} */ var E2 = {B: 2};" +
        "/** @type {E1} */ var x = E2.B;",
        "initializing variable\n" +
        "found   : E2<number>\n" +
        "required: E1<number>");
  }