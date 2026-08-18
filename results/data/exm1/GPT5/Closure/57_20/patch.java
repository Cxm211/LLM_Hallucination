private static String extractClassNameIfGoog(Node node, Node parent, String functionName){
    String className = null;
    if (parent != null && NodeUtil.isExprCall(parent)) {
      Node callee = (node != null) ? node.getFirstChild() : null;
      if (callee != null && callee.getType() == Token.GETPROP) {
        String qualifiedName = callee.getQualifiedName();
        if (functionName.equals(qualifiedName)) {
          Node target = callee.getNext();
          if (target != null) {
            if (target.getType() == Token.STRING) {
              className = target.getString();
            } else {
              className = target.getQualifiedName();
            }
          }
        }
      }
    }
    return className;
  }