  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    if (NodeUtil.isExprCall(parent)) {
      Node call = parent.getFirstChild();
      if (call != null) {
        Node callee = call.getFirstChild();
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
    }
    return className;
  }