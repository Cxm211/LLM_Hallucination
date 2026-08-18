// ===== FIXED com.google.javascript.jscomp.type.ChainableReverseAbstractInterpreter :: declareNameInScope(FlowScope, Node, JSType) [lines 157-180] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-19-fixed/src/com/google/javascript/jscomp/type/ChainableReverseAbstractInterpreter.java =====
  protected void declareNameInScope(FlowScope scope, Node node, JSType type) {
    switch (node.getType()) {
      case Token.NAME:
        scope.inferSlotType(node.getString(), type);
        break;

      case Token.GETPROP:
        String qualifiedName = node.getQualifiedName();
        Preconditions.checkNotNull(qualifiedName);

        JSType origType = node.getJSType();
        origType = origType == null ? getNativeType(UNKNOWN_TYPE) : origType;
        scope.inferQualifiedSlot(node, qualifiedName, origType, type);
        break;

      case Token.THIS:
        // "this" references aren't currently modeled in the CFG.
        break;

      default:
        throw new IllegalArgumentException("Node cannot be refined. \n" +
            node.toStringTree());
    }
  }
