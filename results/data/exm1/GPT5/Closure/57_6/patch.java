  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    if (NodeUtil.isExprCall(parent)) {
      Node callee = node.getFirstChild();
      if (callee != null && (callee.getType() == Token.GETPROP || callee.getType() == Token.NAME)) {
        String qualifiedName = callee.getQualifiedName();
        if (functionName.equals(qualifiedName)) {
          Node target = callee.getNext();
          if (target != null) {
            String qTarget = target.getQualifiedName();
            className = qTarget != null ? qTarget : target.getString();
          }
        }
      }
    }
    return className;
  }