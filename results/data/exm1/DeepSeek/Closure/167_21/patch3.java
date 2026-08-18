private FlowScope maybeRestrictTwoNames(FlowScope blindScope, Node left, boolean leftIsRefineable, JSType restrictedLeftType, Node right, boolean rightIsRefineable, JSType restrictedRightType) {
    boolean shouldRefineLeft = leftIsRefineable && restrictedLeftType != null && !restrictedLeftType.isNoType();
    boolean shouldRefineRight = rightIsRefineable && restrictedRightType != null && !restrictedRightType.isNoType();
    if (shouldRefineLeft || shouldRefineRight) {
      FlowScope informed = blindScope.createChildFlowScope();
      if (shouldRefineLeft) {
        declareNameInScope(informed, left, restrictedLeftType);
      }
      if (shouldRefineRight) {
        declareNameInScope(informed, right, restrictedRightType);
      }
      return informed;
    }
    return blindScope;
  }