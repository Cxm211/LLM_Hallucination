public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    // If the requested outcome is not possible, this path is unreachable.
    if (!literals.contains(outcome)) {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
    // If both true and false are possible, we cannot refine based solely on
    // truthiness here. Signal no refinement with null so callers can decide.
    boolean canBeTrue = literals.contains(true);
    boolean canBeFalse = literals.contains(false);
    if (canBeTrue && canBeFalse) {
      return null;
    }
    // Only the requested outcome is possible; keep the current type.
    return this;
  }