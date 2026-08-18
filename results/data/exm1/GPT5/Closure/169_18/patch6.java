public final boolean isInvariant(JSType that) {
    // Unknown should be considered invariant with any type.
    if (this.isUnknownType() || that.isUnknownType()) {
      return true;
    }
    return checkEquivalenceHelper(that, false);
  }