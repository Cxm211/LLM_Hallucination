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
    // If left cannot satisfy the condition, do not evaluate right side.
    if (restrictedLeftType.equals(getNativeType(JSTypeNative.NO_TYPE))) {
      // Only refine left if refinable.
      if (leftIsRefineable) {
        FlowScope informed = blindScope.createChildFlowScope();
        declareNameInScope(informed, left, restrictedLeftType);
        return informed;
      }
      return blindScope;
    }

    // right type
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
      // If right cannot satisfy the condition, do not refine right.
      if (restrictedRightType != null && restrictedRightType.equals(getNativeType(JSTypeNative.NO_TYPE))) {
        restrictedRightType = null;
      }
      // creating new scope
      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, restrictedLeftType,
          right, rightIsRefineable, restrictedRightType);
    }
    return blindScope;
  }