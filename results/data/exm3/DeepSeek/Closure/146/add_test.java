// com/google/javascript/jscomp/SemanticReverseAbstractInterpreterTest.java
public void testEqCondition5() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        createVar(blind, "a", NULL_TYPE),
        createVar(blind, "b", NULL_TYPE),
        Sets.newHashSet(
            new TypedName("a", NULL_TYPE),
            new TypedName("b", NULL_TYPE)),
        Sets.newHashSet(
            new TypedName("a", NO_TYPE),
            new TypedName("b", NO_TYPE)));
  }
