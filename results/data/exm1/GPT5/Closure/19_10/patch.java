  protected void declareNameInScope(FlowScope scope, Node node, JSType type) {
    switch (node.getType()) {
      case Token.NAME:
        scope.inferSlotType(node.getString(), type);
        break;

      case Token.GETPROP:
        String qualifiedName = node.getQualifiedName();
        Preconditions.checkNotNull(qualifiedName);

        // Use the owner (root) type of the qualified name, not the property's type.
        Node root = node.getFirstChild();
        JSType origType = root != null ? root.getJSType() : null;
        origType = origType == null ? getNativeType(UNKNOWN_TYPE) : origType;
        scope.inferQualifiedSlot(node, qualifiedName, origType, type);
        break;

      default:
        throw new IllegalArgumentException("Node cannot be refined. \n" +
            node.toStringTree());
    }
  }