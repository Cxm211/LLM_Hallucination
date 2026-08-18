private FlowScope caseEquality(Node left, Node right, FlowScope blindScope,
      Function<TypePair, TypePair> merging) {
    JSType leftType = getTypeIfRefinable(left, blindScope);
    boolean leftIsRefineable;
    if (leftType != null) {
      leftIsRefineable = true;
    } else {
      leftIsRefineable = false;
      leftType = left.getJSType();
    }

    JSType rightType = getTypeIfRefinable(right, blindScope);
    boolean rightIsRefineable;
    if (rightType != null) {
      rightIsRefineable = true;
    } else {
      rightIsRefineable = false;
      rightType = right.getJSType();
    }

    TypePair merged = merging.apply(new TypePair(leftType, rightType));

    if (merged != null) {
      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, merged.typeA,
          right, rightIsRefineable, merged.typeB);
    }
    return blindScope;
  }