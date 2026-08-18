  boolean checkUnionEquivalenceHelper(
      UnionType that, boolean tolerateUnknowns) {
    if (!tolerateUnknowns
        && alternates.size() != that.alternates.size()) {
      return false;
    }
    for (JSType alternate : that.alternates) {
      if (!hasAlternate(alternate, tolerateUnknowns)) {
        return false;
      }
    }
    // Also check that all alternates of this are in that
    for (JSType alternate : alternates) {
      if (!that.hasAlternate(alternate, tolerateUnknowns)) {
        return false;
      }
    }
    return true;
  }