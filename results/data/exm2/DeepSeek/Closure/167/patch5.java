  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {
    // Check if this is the unknown type.
    if (this.equals(getNativeType(JSTypeNative.UNKNOWN_TYPE))) {
        if (outcome) {
            return getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE);
        } else {
            return this;
        }
    }
    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (literals.contains(outcome)) {
        return this;
    } else {
        return getNativeType(JSTypeNative.NO_TYPE);
    }
  }