  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (!literals.contains(outcome)) {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
    // If both true and false are possible, no refinement is possible.
    if (literals.contains(true) && literals.contains(false)) {
      return null;
    }
    return this;
  }
