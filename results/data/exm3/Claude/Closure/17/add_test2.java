// com/google/javascript/jscomp/TypeCheckTest.java
public void testConstWithObjectLiteralNoAnnotation() throws Exception {
    testTypes(
        "/** @const */ var OBJ = {a: 1, b: 2};\n" +
        "/** @return {string} */ function h() { return OBJ; }",
        "inconsistent return type\n" +
        "found   : {a: number, b: number}\n" +
        "required: string");
  }