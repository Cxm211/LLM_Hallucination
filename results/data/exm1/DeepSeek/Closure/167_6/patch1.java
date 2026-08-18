  private FlowScope caseAndOrNotShortCircuiting(Node left, Node right,
        FlowScope blindScope, boolean condition) {
    // left type
    JSType leftType = getTypeIfRefinable(left, blindScope);
    boolean leftIsRefineable = leftType != null;
    if (!leftIsRefineable) {
      leftType = left.getJSType();
    }
    blindScope = firstPreciserScopeKnowingConditionOutcome(
        left, blindScope, condition);

    // restricting left type
    JSType restrictedLeftType = (leftType == null) ? null :
        leftType.getRestrictedTypeGivenToBooleanOutcome(condition);
    if (restrictedLeftType == null) {
      return firstPreciserScopeKnowingConditionOutcome(
          right, blindScope, condition);
    }

    // right type
    JSType rightType = getTypeIfRefinable(right, blindScope);
    boolean rightIsRefineable = rightType != null;
    if (!rightIsRefineable) {
      rightType = right.getJSType();
    }
    blindScope = firstPreciserScopeKnowingConditionOutcome(
        right, blindScope, condition);

    if (condition) {
      JSType restrictedRightType = (rightType == null) ? null :
          rightType.getRestrictedTypeGivenToBooleanOutcome(condition);

      // creating new scope
      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, restrictedLeftType,
          right, rightIsRefineable, restrictedRightType);
    }
    return blindScope;
  }