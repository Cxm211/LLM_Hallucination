  private FlowScope caseEquality(Node left, Node right, FlowScope blindScope,
      Function<TypePair, TypePair> merging) {
    // left type
    JSType leftType = getTypeIfRefinable(left, blindScope);
    boolean leftIsRefineable;
    if (leftType != null) {
      leftIsRefineable = true;
    } else {
      leftIsRefineable = false;
      leftType = left.getJSType();
    }

    // right type
    JSType rightType = getTypeIfRefinable(right, blindScope);
    boolean rightIsRefineable;
    if (rightType != null) {
      rightIsRefineable = true;
    } else {
      rightIsRefineable = false;
      rightType = right.getJSType();
    }

    // merged types
    TypePair merged = merging.apply(new TypePair(leftType, rightType));

    // creating new scope
    if (merged != null) {
      JSType noType = getNativeType(JSTypeNative.NO_TYPE);
      JSType restrictedLeft = merged.typeA;
      JSType restrictedRight = merged.typeB;
      if (noType.equals(restrictedLeft)) {
        restrictedLeft = null;
      }
      if (noType.equals(restrictedRight)) {
        restrictedRight = null;
      }
      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, restrictedLeft,
          right, rightIsRefineable, restrictedRight);
    }
    return blindScope;
  }