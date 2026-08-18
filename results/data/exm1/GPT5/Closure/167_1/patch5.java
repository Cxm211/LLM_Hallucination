  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (outcome) {
      if (literals.contains(true)) {
        JSType truthy = getTruthyType();
        return truthy != null ? truthy : getNativeType(JSTypeNative.NO_TYPE);
      } else {
        return getNativeType(JSTypeNative.NO_TYPE);
      }
    } else {
      if (literals.contains(false)) {
        JSType falsy = getFalsyType();
        return falsy != null ? falsy : getNativeType(JSTypeNative.NO_TYPE);
      } else {
        return getNativeType(JSTypeNative.NO_TYPE);
      }
    }
  }