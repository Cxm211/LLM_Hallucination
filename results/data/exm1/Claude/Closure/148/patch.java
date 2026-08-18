private Node tryFoldTypeof(Node originalTypeofNode) {
    Preconditions.checkArgument(originalTypeofNode.getType() == Token.TYPEOF);

    Node argumentNode = originalTypeofNode.getFirstChild();
    if (argumentNode == null) {
      return originalTypeofNode;
    }

    // Handle 'undefined' NAME node specially before checking isLiteralValue
    if (argumentNode.getType() == Token.NAME && 
        "undefined".equals(argumentNode.getString())) {
      Node newNode = Node.newString("undefined");
      originalTypeofNode.getParent().replaceChild(originalTypeofNode, newNode);
      reportCodeChange();
      return newNode;
    }

    if (!NodeUtil.isLiteralValue(argumentNode)) {
      return originalTypeofNode;
    }

    String typeNameString = null;

    switch (argumentNode.getType()) {
      case Token.STRING:
        typeNameString = "string";
        break;
      case Token.NUMBER:
        typeNameString = "number";
        break;
      case Token.TRUE:
      case Token.FALSE:
        typeNameString = "boolean";
        break;
      case Token.NULL:
      case Token.OBJECTLIT:
      case Token.ARRAYLIT:
        typeNameString = "object";
        break;
    }

    if (typeNameString != null) {
      Node newNode = Node.newString(typeNameString);
      originalTypeofNode.getParent().replaceChild(originalTypeofNode, newNode);
      reportCodeChange();

      return newNode;
    }

    return originalTypeofNode;
  }