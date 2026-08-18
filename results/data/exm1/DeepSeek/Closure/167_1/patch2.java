  private FlowScope maybeRestrictName(
      FlowScope blindScope, Node node, JSType originalType, JSType restrictedType) {
    JSType noType = getNativeType(JSTypeNative.NO_TYPE);
    if (restrictedType != null && !restrictedType.equals(originalType) && !restrictedType.equals(noType)) {
      FlowScope informed = blindScope.createChildFlowScope();
      declareNameInScope(informed, node, restrictedType);
      return informed;
    }
    return blindScope;
  }