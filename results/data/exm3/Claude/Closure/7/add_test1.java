// com/google/javascript/jscomp/SemanticReverseAbstractInterpreterTest.java
public void testTypeof4() {
  FlowScope blind = newScope();
  testBinop(blind,
      Token.NE,
      new Node(Token.TYPEOF, createVar(
          blind, "a", OBJECT_NUMBER_STRING_BOOLEAN)),
      Node.newString("function"),
      Sets.newHashSet(
          new TypedName("a", OBJECT_NUMBER_STRING_BOOLEAN)),
      Sets.newHashSet(
          new TypedName("a", U2U_CONSTRUCTOR_TYPE)));
}