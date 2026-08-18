// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testStructuralConstructorUnion() throws Exception {
    JSType type = testParseType(
        "function (new:(Object|string))",
        "function (): (Object|string)");
    assertTrue(type.isConstructor());
    assertFalse(type.isNominalConstructor());
}
