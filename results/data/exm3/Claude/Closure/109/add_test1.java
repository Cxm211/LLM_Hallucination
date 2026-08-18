// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testStructuralConstructor5() throws Exception {
    JSType type = testParseType(
        "function (new:!Object)",
        "function (): Object");
    assertTrue(type.isConstructor());
    assertFalse(type.isNominalConstructor());
}