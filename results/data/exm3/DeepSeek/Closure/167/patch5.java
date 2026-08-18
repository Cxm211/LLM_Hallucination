  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {
    // Handle union types
    if (this instanceof UnionType) {
      UnionType union = (UnionType) this;
      List<JSType> alternates = union.getAlternates();
      List<JSType> restricted = new ArrayList<>();
      for (JSType alt : alternates) {
        JSType restrictedAlt = alt.getRestrictedTypeGivenToBooleanOutcome(outcome);
        if (!restrictedAlt.equals(getNativeType(JSTypeNative.NO_TYPE))) {
          restricted.add(restrictedAlt);
        }
      }
      if (restricted.isEmpty()) {
        return getNativeType(JSTypeNative.NO_TYPE);
      } else if (restricted.size() == 1) {
        return restricted.get(0);
      } else {
        return new UnionType.Builder(registry).addAlternates(restricted).build();
      }
    }
    // Handle unknown type
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