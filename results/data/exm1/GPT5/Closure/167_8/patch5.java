public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    boolean canBeOutcome = literals.contains(outcome);
    boolean canBeOpposite = literals.contains(!outcome);
    if (!canBeOutcome) {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
    // If both outcomes are possible, we cannot refine based on this information.
    if (canBeOpposite) {
      return null;
    }
    // Only the given outcome is possible; keep current type.
    return this;
  }