  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    if (NodeUtil.isExprCall(parent)) {
      if (node.getType() == Token.GETPROP) {
        String qualifiedName = node.getQualifiedName();
        if (functionName.equals(qualifiedName)) {
          Node target = node.getNext();
          if (target != null) {
            className = target.getString();
          }
        }
      }
    }
    return className;
  }