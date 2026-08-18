  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    // Special-case unknown: restricting true to CHECKED_UNKNOWN while
    // keeping false as UNKNOWN.
    if (this.equals(getNativeType(JSTypeNative.UNKNOWN_TYPE))) {
      return outcome ? getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE) : this;
    }

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (literals.contains(outcome)) {
      return this;
    } else {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
  }