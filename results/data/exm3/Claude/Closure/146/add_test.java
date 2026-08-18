// com/google/javascript/jscomp/SemanticReverseAbstractInterpreterTest.java
public void testEqCondition_NumberAndString() throws Exception {
  FlowScope blind = newScope();
  testBinop(blind,
      Token.EQ,
      createVar(blind, "a", NUMBER_TYPE),
      createVar(blind, "b", STRING_TYPE),
      Sets.newHashSet(
          new TypedName("a", NO_TYPE),
          new TypedName("b", NO_TYPE)),
      Sets.newHashSet(
          new TypedName("a", NUMBER_TYPE),
          new TypedName("b", STRING_TYPE)));
}