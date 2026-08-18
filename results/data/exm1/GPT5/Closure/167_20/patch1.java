  private FlowScope caseAndOrNotShortCircuiting(Node left, Node right,
        FlowScope blindScope, boolean condition) {
    // left type
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

    // restricting left type
    JSType restrictedLeftType = (leftType == null) ? null :
        leftType.getRestrictedTypeGivenToBooleanOutcome(condition);
    if (restrictedLeftType == null) {
      return firstPreciserScopeKnowingConditionOutcome(
          right, blindScope, condition);
    }

    // If the overall condition must be true, refine the scope with the
    // information learned from the left before analyzing the right.
    FlowScope scopeForRight = blindScope;
    if (condition && leftIsRefineable) {
      scopeForRight = maybeRestrictName(blindScope, left, leftType, restrictedLeftType);
    }

    // right type
    JSType rightType = getTypeIfRefinable(right, scopeForRight);
    boolean rightIsRefineable;
    if (rightType != null) {
      rightIsRefineable = true;
    } else {
      rightIsRefineable = false;
      rightType = right.getJSType();
      scopeForRight = firstPreciserScopeKnowingConditionOutcome(
          right, scopeForRight, condition);
    }

    if (condition) {
      JSType restrictedRightType = (rightType == null) ? null :
          rightType.getRestrictedTypeGivenToBooleanOutcome(condition);

      // creating new scope
      return maybeRestrictTwoNames(
          scopeForRight,
          left, leftIsRefineable, restrictedLeftType,
          right, rightIsRefineable, restrictedRightType);
    }
    return blindScope;
  }