  protected void declareNameInScope(FlowScope scope, Node node, JSType type) {
    switch (node.getType()) {
      case Token.NAME:
        scope.inferSlotType(node.getString(), type);
        break;

      case Token.GETPROP:
        // Do not attempt to refine properties on 'this' as they aren't modeled in the CFG.
        Node owner = node.getFirstChild();
        if (owner != null && owner.getType() == Token.THIS) {
          break;
        }
        String qualifiedName = node.getQualifiedName();
        if (qualifiedName == null) {
          // Not a simple qualified name; nothing to refine.
          break;
        }

        JSType origType = node.getJSType();
        origType = origType == null ? getNativeType(UNKNOWN_TYPE) : origType;
        scope.inferQualifiedSlot(node, qualifiedName, origType, type);
        break;

      // "this" references aren't currently modeled in the CFG.
      case Token.THIS:
        // Nothing to do.
        break;

      default:
        throw new IllegalArgumentException("Node cannot be refined. \n" +
            node.toStringTree());
    }
  }