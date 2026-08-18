public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    boolean canBeOutcome = literals.contains(outcome);
    boolean canBeOpposite = literals.contains(!outcome);

    if (canBeOutcome && canBeOpposite) {
      // No refinement possible when both outcomes are possible.
      return null;
    } else if (canBeOutcome) {
      return this;
    } else {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
  }