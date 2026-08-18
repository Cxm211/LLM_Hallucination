// com/google/javascript/jscomp/SemanticReverseAbstractInterpreterTest.java
public void testTypeof4() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        new Node(Token.TYPEOF, createVar(
            blind, "a", OBJECT_TYPE)),
        Node.newString("function"),
        Sets.newHashSet(
            new TypedName("a", null)),
        Sets.newHashSet(
            new TypedName("a", OBJECT_TYPE)));
  }
