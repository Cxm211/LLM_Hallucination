private boolean hasAlternate(JSType type, boolean tolerateUnknowns) {
    for (JSType alternate : alternates) {
      if (alternate.checkEquivalenceHelper(type, tolerateUnknowns)) {
        return true;
      }
    }
    return false;
  }