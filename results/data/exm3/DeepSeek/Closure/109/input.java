// buggy function
  private Node parseContextTypeExpression(JsDocToken token) {
          return parseTypeName(token);
  }

// trigger testcase
// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java::testStructuralConstructor2
public void testStructuralConstructor2() throws Exception {
    JSType type = testParseType(
        "function (new:?)",
        // toString skips unknowns, but isConstructor reveals the truth.
        "function (): ?");
    assertTrue(type.isConstructor());
    assertFalse(type.isNominalConstructor());
  }

// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java::testStructuralConstructor3
public void testStructuralConstructor3() throws Exception {
    resolve(parse("@type {function (new:*)} */").getType(),
        "constructed type must be an object type");
  }
