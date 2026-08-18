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
            // Prefer qualified name when available (e.g., for GETPROP/NAME),
            // otherwise fall back to string value (for STRING nodes).
            String qName = target.getQualifiedName();
            className = (qName != null) ? qName : target.getString();
          }
        }
      }
    }
    return className;
  }