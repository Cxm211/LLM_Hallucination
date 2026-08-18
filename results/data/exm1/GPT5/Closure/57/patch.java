  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    if (NodeUtil.isExprCall(parent)) {
      Node callee = node.getFirstChild();
      if (callee != null && callee.getType() == Token.GETPROP) {
        String qualifiedName = callee.getQualifiedName();
        if (functionName.equals(qualifiedName)) {
          Node target = callee.getNext();
          if (target != null) {
            switch (target.getType()) {
              case Token.STRING:
                className = target.getString();
                break;
              case Token.NAME:
              case Token.GETPROP:
                className = target.getQualifiedName();
                break;
              default:
                className = null;
            }
          }
        }
      }
    }
    return className;
  }