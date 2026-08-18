  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    if (NodeUtil.isExprCall(parent)) {
      if (node != null && node.getType() == Token.CALL) {
        Node callee = node.getFirstChild();
        if (callee != null && callee.getType() == Token.GETPROP) {
          String qualifiedName = callee.getQualifiedName();
          if (functionName.equals(qualifiedName)) {
            Node target = callee.getNext();
            if (target != null) {
              // Prefer the fully-qualified name when available (e.g. goog.A),
              // otherwise fall back to the raw string (for string literals).
              String targetQname = target.getQualifiedName();
              className = (targetQname != null) ? targetQname : target.getString();
            }
          }
        }
      }
    }
    return className;
  }