  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (!literals.contains(outcome)) {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
    // If both outcomes are possible, no restriction can be made.
    if (literals.contains(true) && literals.contains(false)) {
      return null;
    }
    // Only the requested outcome is possible; keep the current type.
    return this;
  }