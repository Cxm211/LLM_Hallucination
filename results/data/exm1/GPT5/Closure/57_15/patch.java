  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    // Accept any CALL context, not just expression calls.
    if (node != null && node.getType() == Token.CALL) {
      Node callee = node.getFirstChild();
      if (callee != null && callee.getType() == Token.GETPROP) {
        String qualifiedName = callee.getQualifiedName();
        if (functionName.equals(qualifiedName)) {
          Node target = callee.getNext();
          if (target != null && target.getType() == Token.STRING) {
            className = target.getString();
          }
        }
      }
    }
    return className;
  }