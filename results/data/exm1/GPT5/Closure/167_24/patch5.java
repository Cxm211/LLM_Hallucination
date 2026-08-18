public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (!literals.contains(outcome)) {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
    // If both outcomes are possible, preserve the original type except for
    // unknown types when restricting to truthy, which become CHECKED_UNKNOWN.
    if (literals.contains(!outcome)) {
      if (isUnknownType() && outcome) {
        return getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE);
      }
    }
    return this;
  }