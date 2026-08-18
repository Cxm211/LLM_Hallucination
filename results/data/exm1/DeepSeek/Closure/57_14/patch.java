private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    if (NodeUtil.isExprCall(parent)) {
      Node callee = node.getFirstChild();
      if (callee != null) {
        String name = null;
        if (callee.getType() == Token.GETPROP) {
          name = callee.getQualifiedName();
        } else if (callee.getType() == Token.NAME) {
          name = callee.getString();
        }
        if (name != null && functionName.equals(name)) {
          Node target = callee.getNext();
          if (target != null) {
            className = target.getString();
          }
        }
      }
    }
    return className;
  }