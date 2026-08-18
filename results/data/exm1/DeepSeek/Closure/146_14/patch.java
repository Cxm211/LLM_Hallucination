  public TypePair getTypesUnderInequality(JSType that) {
    // unions types
    if (that instanceof UnionType) {
      UnionType unionThat = (UnionType) that;
      JSType resultTypeA = null;
      JSType resultTypeB = null;
      for (JSType alternate : unionThat.getAlternates()) {
        TypePair p = this.getTypesUnderInequality(alternate);
        if (resultTypeA == null) {
          resultTypeA = p.typeA;
          resultTypeB = p.typeB;
        } else {
          resultTypeA = resultTypeA.union(p.typeA);
          resultTypeB = resultTypeB.union(p.typeB);
        }
      }
      return new TypePair(resultTypeA, resultTypeB);
    }

    // other types
    switch (this.testForEquality(that)) {
      case TRUE:
        return new TypePair(null, null);

      case FALSE:
      case UNKNOWN:
        return new TypePair(this, that);
    }

    // switch case is exhaustive
    throw new IllegalStateException();
  }