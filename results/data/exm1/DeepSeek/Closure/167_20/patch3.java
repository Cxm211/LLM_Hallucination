private FlowScope maybeRestrictTwoNames(
    FlowScope blindScope,
    Node left, boolean leftIsRefineable, JSType rawLeftType, JSType restrictedLeftType,
    Node right, boolean rightIsRefineable, JSType rawRightType, JSType restrictedRightType) {
  boolean shouldRefineLeft =
      leftIsRefineable && restrictedLeftType != null && !restrictedLeftType.equals(rawLeftType) && !restrictedLeftType.isNoType();
  boolean shouldRefineRight =
      rightIsRefineable && restrictedRightType != null && !restrictedRightType.equals(rawRightType) && !restrictedRightType.isNoType();
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