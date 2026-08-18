  public final boolean differsFrom(JSType that) {
    return !checkEquivalenceHelper(that, true);
  }