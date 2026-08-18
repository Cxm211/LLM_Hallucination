private FlowScope maybeRestrictTwoNames(
    FlowScope blindScope,
    Node left, JSType originalLeftType, boolean leftIsRefineable, JSType restrictedLeftType,
    Node right, JSType originalRightType, boolean rightIsRefineable, JSType restrictedRightType) {
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