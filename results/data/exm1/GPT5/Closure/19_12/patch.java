  protected void declareNameInScope(FlowScope scope, Node node, JSType type) {
    switch (node.getType()) {
      case Token.NAME:
        scope.inferSlotType(node.getString(), type);
        break;

      case Token.GETPROP:
        String qualifiedName = node.getQualifiedName();
        // If the qualified name cannot be determined (e.g. obj is a CALL),
        // we cannot refine it safely. Just bail out gracefully.
        if (qualifiedName == null) {
          break;
        }

        JSType origType = node.getJSType();
        origType = origType == null ? getNativeType(UNKNOWN_TYPE) : origType;
        scope.inferQualifiedSlot(node, qualifiedName, origType, type);
        break;

        // "this" references aren't currently modeled in the CFG.

      default:
        throw new IllegalArgumentException("Node cannot be refined. \n" +
            node.toStringTree());
    }
  }