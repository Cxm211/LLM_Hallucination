  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (!literals.contains(outcome)) {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
    // If both outcomes are possible and the condition is true, refine by
    // removing null/undefined, which are the common falsy nullable cases.
    // For the false outcome (or when already precise), keep the original type.
    if (literals.contains(true) && literals.contains(false)) {
      if (outcome) {
        // Prefer a standard refinement that drops null/undefined if available.
        // If not available in this environment, fall back to the original type.
        JSType refined = restrictByNotNullOrUndefined();
        return refined == null ? this : refined;
      }
      return this;
    }
    return this;
  }
