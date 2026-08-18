  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    // The check must operate on the CALL node itself, not only when the
    // parent is an EXPR_RESULT. Calls often appear in expressions (if, return,
    // &&, ?:), so handle any CALL context.
    if (node != null && NodeUtil.isCall(node)) {
      Node callee = node.getFirstChild();
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
    return className;
  }