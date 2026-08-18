// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testStructuralConstructorRecord() throws Exception {
    JSType type = testParseType(
        "function (new:{x: number})",
        "function (): {x: number}");
    assertTrue(type.isConstructor());
    assertFalse(type.isNominalConstructor());
}
