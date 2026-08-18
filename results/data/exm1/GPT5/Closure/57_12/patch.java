  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    // The check should be on the call node itself, not its parent.
    if (NodeUtil.isExprCall(node)) {
      Node callee = node.getFirstChild();
      if (callee != null && callee.getType() == Token.GETPROP) {
        String qualifiedName = callee.getQualifiedName();
        if (functionName.equals(qualifiedName)) {
          Node target = callee.getNext();
          if (target != null) {
            className = target.getString();
          }
        }
      }
    }
    return className;
  }