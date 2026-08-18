  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    if (NodeUtil.isExprCall(parent)) {
      Node callee = node.getFirstChild();
      if (callee != null) {
        int type = callee.getType();
        if (type == Token.GETPROP || type == Token.NAME) {
          String qualifiedName = callee.getQualifiedName();
          if (functionName.equals(qualifiedName)) {
            Node target = callee.getNext();
            if (target != null) {
              className = target.getString();
            }
          }
        }
      }
    }
    return className;
  }