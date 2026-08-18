// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testStructuralConstructor4() throws Exception {
    JSType type = testParseType(
        "function (new:undefined)",
        "function (): undefined");
    assertTrue(type.isConstructor());
    assertFalse(type.isNominalConstructor());
}