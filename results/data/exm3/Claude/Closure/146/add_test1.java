// com/google/javascript/jscomp/SemanticReverseAbstractInterpreterTest.java
public void testEqCondition_NullAndVoid() throws Exception {
  FlowScope blind = newScope();
  testBinop(blind,
      Token.EQ,
      createVar(blind, "a", NULL_TYPE),
      createVar(blind, "b", VOID_TYPE),
      Sets.newHashSet(
          new TypedName("a", NULL_TYPE),
          new TypedName("b", VOID_TYPE)),
      Sets.newHashSet(
          new TypedName("a", NULL_TYPE),
          new TypedName("b", VOID_TYPE)));
}