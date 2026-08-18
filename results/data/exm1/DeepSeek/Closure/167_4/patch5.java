  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {
    if (this == null) {
      return null;
    }
    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (literals.contains(outcome)) {
      return this;
    } else {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
  }