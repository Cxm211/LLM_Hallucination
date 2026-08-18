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
    JSType noType = getNativeType(JSTypeNative.NO_TYPE);
    if (noType.equals(restrictedLeftType)) {
      restrictedLeftType = null;
    }
    if (restrictedLeftType == null) {
      return firstPreciserScopeKnowingConditionOutcome(
          right, blindScope, condition);
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
      if (noType.equals(restrictedRightType)) {
        restrictedRightType = null;
      }

      // creating new scope
      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, restrictedLeftType,
          right, rightIsRefineable, restrictedRightType);
    } else {
      // For || operator with false condition, restrict left to false, right is unrestricted
      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, restrictedLeftType,
          right, false, null);
    }
  }