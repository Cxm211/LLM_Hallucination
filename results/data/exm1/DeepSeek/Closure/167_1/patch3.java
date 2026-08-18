  private FlowScope maybeRestrictTwoNames(
      FlowScope blindScope,
      Node left, boolean leftIsRefineable, JSType restrictedLeftType,
      Node right, boolean rightIsRefineable, JSType restrictedRightType) {
    JSType noType = getNativeType(JSTypeNative.NO_TYPE);
    boolean shouldRefineLeft =
        leftIsRefineable && restrictedLeftType != null && !noType.equals(restrictedLeftType);
    boolean shouldRefineRight =
        rightIsRefineable && restrictedRightType != null && !noType.equals(restrictedRightType);
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