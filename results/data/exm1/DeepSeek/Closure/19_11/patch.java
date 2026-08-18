protected void declareNameInScope(FlowScope scope, Node node, JSType type) {
    switch (node.getType()) {
      case Token.NAME:
        scope.inferSlotType(node.getString(), type);
        break;

      case Token.GETPROP:
        String qualifiedName = node.getQualifiedName();
        Preconditions.checkNotNull(qualifiedName);

        JSType origType = node.getJSType();
        scope.inferQualifiedSlot(node, qualifiedName, origType, type);
        break;

      default:
        throw new IllegalArgumentException("Node cannot be refined. \n" +
            node.toStringTree());
    }
  }