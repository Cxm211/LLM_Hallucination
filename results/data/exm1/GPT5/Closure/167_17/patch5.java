public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (!literals.contains(outcome)) {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
    // If both outcomes are possible, try to refine away values that do not
    // match the requested boolean outcome. If no refinement is possible,
    // return the original type.
    JSType refined = getTypeWhenOutcomeIs(outcome);
    return refined != null ? refined : this;
  }