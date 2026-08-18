  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (!literals.contains(outcome)) {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
    // If both boolean outcomes are possible, there is no refinement.
    if (literals.contains(!outcome)) {
      return null;
    }
    return this;
  }