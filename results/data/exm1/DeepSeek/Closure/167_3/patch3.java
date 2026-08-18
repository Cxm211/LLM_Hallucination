private FlowScope maybeRestrictTwoNames(
      FlowScope blindScope,
      Node left, boolean leftIsRefineable, JSType originalLeftType, JSType restrictedLeftType,
      Node right, boolean rightIsRefineable, JSType originalRightType, JSType restrictedRightType) {
    boolean shouldRefineLeft =
        leftIsRefineable && restrictedLeftType != null && !restrictedLeftType.equals(originalLeftType);
    boolean shouldRefineRight =
        rightIsRefineable && restrictedRightType != null && !restrictedRightType.equals(originalRightType);
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