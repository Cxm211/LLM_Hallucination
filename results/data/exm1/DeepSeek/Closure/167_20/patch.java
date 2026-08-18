private FlowScope caseEquality(Node left, Node right, FlowScope blindScope, Function<TypePair, TypePair> merging) {
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
    }

    TypePair merged = merging.apply(new TypePair(leftType, rightType));

    if (merged != null) {
      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, rawLeftType, merged.typeA,
          right, rightIsRefineable, rawRightType, merged.typeB);
    }
    return blindScope;
  }