  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    if (NodeUtil.isExprCall(parent)) {
      Node callee = node.getFirstChild();
      if (callee != null && callee.getType() == Token.GETPROP) {
        String qualifiedName = callee.getQualifiedName();
        if (functionName.equals(qualifiedName)) {
          Node firstArg = callee.getNext();
          if (firstArg != null) {
            Node secondArg = firstArg.getNext();
            if (secondArg != null && secondArg.getType() == Token.ARRAYLIT) {
              Node classNameNode = secondArg.getFirstChild();
              if (classNameNode != null && classNameNode.getType() == Token.STRING) {
                className = classNameNode.getString();
              }
            }
          }
        }
      }
    }
    return className;
  }