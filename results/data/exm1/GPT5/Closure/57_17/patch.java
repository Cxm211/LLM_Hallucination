  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    // Case 1: Called when node is the CALL, and parent is the EXPR_RESULT
    if (parent != null && NodeUtil.isExprCall(parent) && node != null && node.getType() == Token.CALL) {
      Node callee = node.getFirstChild();
      if (callee != null && callee.getType() == Token.GETPROP) {
        String qualifiedName = callee.getQualifiedName();
        if (functionName.equals(qualifiedName)) {
          Node target = callee.getNext();
          if (target != null) {
            String qName = target.getQualifiedName();
            className = (qName != null) ? qName : target.getString();
          }
        }
      }
      return className;
    }

    // Case 2: Called when node is the GETPROP callee, and parent is the CALL
    if (parent != null && parent.getType() == Token.CALL && node != null && node.getType() == Token.GETPROP) {
      String qualifiedName = node.getQualifiedName();
      if (functionName.equals(qualifiedName)) {
        Node target = node.getNext();
        if (target != null) {
          String qName = target.getQualifiedName();
          className = (qName != null) ? qName : target.getString();
        }
      }
    }
    return className;
  }