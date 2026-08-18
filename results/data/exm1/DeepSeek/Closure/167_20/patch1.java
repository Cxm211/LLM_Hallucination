private FlowScope caseAndOrNotShortCircuiting(Node left, Node right, FlowScope blindScope, boolean condition) {
    JSType leftType = getTypeIfRefinable(left, blindScope);
    boolean leftIsRefineable;
    JSType rawLeftType;
    if (leftType != null) {
      leftIsRefineable = true;
      rawLeftType = leftType;
    } else {
      leftIsRefineable = false;
      leftType = left.getJSType();
      rawLeftType = leftType;
      blindScope = firstPreciserScopeKnowingConditionOutcome(left, blindScope, condition);
    }

    JSType restrictedLeftType = (leftType == null) ? null :
        leftType.getRestrictedTypeGivenToBooleanOutcome(condition);
    if (restrictedLeftType == null) {
      return firstPreciserScopeKnowingConditionOutcome(right, blindScope, condition);
    }

    JSType rightType = getTypeIfRefinable(right, blindScope);
    boolean rightIsRefineable;
    JSType rawRightType;
    if (rightType != null) {
      rightIsRefineable = true;
      rawRightType = rightType;
    } else {
      rightIsRefineable = false;
      rightType = right.getJSType();
      rawRightType = rightType;
      blindScope = firstPreciserScopeKnowingConditionOutcome(right, blindScope, condition);
    }

    if (condition) {
      JSType restrictedRightType = (rightType == null) ? null :
          rightType.getRestrictedTypeGivenToBooleanOutcome(condition);

      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, rawLeftType, restrictedLeftType,
          right, rightIsRefineable, rawRightType, restrictedRightType);
    }
    return blindScope;
  }