public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (literals.contains(outcome)) {
      if (isUnknownType() && outcome) {
        return getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE);
      }
      return this;
    } else {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
  }