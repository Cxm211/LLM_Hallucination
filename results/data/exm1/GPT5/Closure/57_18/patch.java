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
            // Use the qualified name when available (e.g., goog.Foo),
            // fallback to simple string for NAME nodes.
            String qName = target.getQualifiedName();
            if (qName != null) {
              className = qName;
            } else {
              className = target.getString();
            }
          }
        }
      }
    }
    return className;
  }