  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    if (NodeUtil.isExprCall(parent)) {
      // Ensure we are analyzing the CALL node under the expression
      Node call = parent.getFirstChild();
      if (call != null) {
        Node callee = call.getFirstChild();
        if (callee != null && callee.getType() == Token.GETPROP) {
          String qualifiedName = callee.getQualifiedName();
          if (functionName.equals(qualifiedName)) {
            Node target = callee.getNext();
            if (target != null) {
              // Use qualified name to handle both NAME and GETPROP arguments
              className = target.getQualifiedName();
            }
          }
        }
      }
    }
    return className;
  }