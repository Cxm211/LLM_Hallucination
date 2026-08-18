private FlowScope caseAndOrNotShortCircuiting(Node left, Node right,
        FlowScope blindScope, boolean condition) {
    JSType leftType = getTypeIfRefinable(left, blindScope);
    boolean leftIsRefineable;
    if (leftType != null) {
      leftIsRefineable = true;
    } else {
      leftIsRefineable = false;
      leftType = left.getJSType();
      blindScope = firstPreciserScopeKnowingConditionOutcome(
          left, blindScope, condition);
    }

    JSType restrictedLeftType = (leftType == null) ? null :
        leftType.getRestrictedTypeGivenToBooleanOutcome(condition);
    if (restrictedLeftType == null) {
      return firstPreciserScopeKnowingConditionOutcome(
          right, blindScope, condition);
    }

    JSType rightType = getTypeIfRefinable(right, blindScope);
    boolean rightIsRefineable;
    if (rightType != null) {
      rightIsRefineable = true;
    } else {
      rightIsRefineable = false;
      rightType = right.getJSType();
      blindScope = firstPreciserScopeKnowingConditionOutcome(
          right, blindScope, condition);
    }

    if (condition) {
      JSType restrictedRightType = (rightType == null) ? null :
          rightType.getRestrictedTypeGivenToBooleanOutcome(condition);

      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, restrictedLeftType,
          right, rightIsRefineable, restrictedRightType);
    } else {
      return maybeRestrictName(blindScope, left, leftType, restrictedLeftType);
    }
  }