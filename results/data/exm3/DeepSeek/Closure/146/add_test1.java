// com/google/javascript/jscomp/SemanticReverseAbstractInterpreterTest.java
public void testNeConditionWithUnion() throws Exception {
    FlowScope blind = newScope();
    JSType unionType = createUnionType(VOID_TYPE, NULL_TYPE);
    testBinop(blind,
        Token.NE,
        createVar(blind, "a", unionType),
        createVar(blind, "b", VOID_TYPE),
        Sets.newHashSet(
            new TypedName("a", NO_TYPE),
            new TypedName("b", NO_TYPE)),
        Sets.newHashSet(
            new TypedName("a", unionType),
            new TypedName("b", VOID_TYPE)));
  }
