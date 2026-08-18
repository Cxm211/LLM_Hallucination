  protected void declareNameInScope(FlowScope scope, Node node, JSType type) {
    switch (node.getType()) {
      case Token.NAME:
        scope.inferSlotType(node.getString(), type);
        break;

      case Token.GETPROP:
        String qualifiedName = node.getQualifiedName();
        Preconditions.checkNotNull(qualifiedName);

        // Use the owner (object) type for property refinement, not the property type itself.
        JSType ownerType = node.getFirstChild() != null ? node.getFirstChild().getJSType() : null;
        ownerType = ownerType == null ? getNativeType(UNKNOWN_TYPE) : ownerType;
        scope.inferQualifiedSlot(node, qualifiedName, ownerType, type);
        break;

        // "this" references aren't currently modeled in the CFG.

      default:
        throw new IllegalArgumentException("Node cannot be refined. \n" +
            node.toStringTree());
    }
  }