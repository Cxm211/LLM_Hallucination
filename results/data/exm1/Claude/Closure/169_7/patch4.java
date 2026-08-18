public boolean hasEqualCallType(FunctionType otherType) {
    return this.call.checkArrowEquivalenceHelper(otherType.call, false);
  }