// com/google/javascript/jscomp/NodeUtilTest.java
public void testLocalValueAssignmentLocalLeft() throws Exception {
    Predicate<Node> localPredicate = new Predicate<Node>() {
      @Override
      public boolean apply(Node n) {
        if (n.getType() == Token.NAME) {
          String name = n.getString();
          return name.equals("localVar") || name.equals("otherLocal");
        }
        return false;
      }
    };
    Node assign = getNode("localVar = otherLocal");
    assertTrue(NodeUtil.evaluatesToLocalValue(assign, localPredicate));
  }
