// com/google/javascript/rhino/jstype/RecordTypeTest.java
public void testHasEqualParametersWithNullTypes() throws Exception {
    // Create parameter list with one parameter with type NUMBER
    Node paramWithType = new Node(Token.NAME);
    paramWithType.setJSType(NUMBER_TYPE);
    Node paramListWithType = new Node(Token.PARAM_LIST, paramWithType);
    ArrowType arrowWithType = new ArrowType(registry, paramListWithType, NUMBER_TYPE, false);
    
    // Create parameter list with one parameter without type (null)
    Node paramWithoutType = new Node(Token.NAME);
    // leave JSType as null
    Node paramListWithoutType = new Node(Token.PARAM_LIST, paramWithoutType);
    ArrowType arrowWithoutType = new ArrowType(registry, paramListWithoutType, NUMBER_TYPE, false);
    
    assertFalse(arrowWithType.hasEqualParameters(arrowWithoutType, false));
    assertFalse(arrowWithoutType.hasEqualParameters(arrowWithType, false));
    // Also test both null types (should be equal)
    Node paramNull1 = new Node(Token.NAME);
    Node paramNull2 = new Node(Token.NAME);
    Node paramListNull1 = new Node(Token.PARAM_LIST, paramNull1);
    Node paramListNull2 = new Node(Token.PARAM_LIST, paramNull2);
    ArrowType arrowNull1 = new ArrowType(registry, paramListNull1, NUMBER_TYPE, false);
    ArrowType arrowNull2 = new ArrowType(registry, paramListNull2, NUMBER_TYPE, false);
    assertTrue(arrowNull1.hasEqualParameters(arrowNull2, false));
  }
