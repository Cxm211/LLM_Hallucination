  public TypePair getTypesUnderInequality(JSType that) {
    // if this is a union type
    if (this instanceof UnionType) {
      UnionType unionThis = (UnionType) this;
      List<JSType> kept = new ArrayList<JSType>();
      for (JSType alt : unionThis.getAlternates()) {
        if (alt.testForEquality(that) != TernaryValue.TRUE) {
          kept.add(alt);
        }
      }
      if (kept.isEmpty()) {
        return new TypePair(NO_TYPE, NO_TYPE);
      } else if (kept.size() == 1) {
        return new TypePair(kept.get(0), that);
      } else {
        return new TypePair(
            unionThis.registry.createUnionType(kept), that);
      }
    }

    // if that is a union type
    if (that instanceof UnionType) {
      TypePair p = that.getTypesUnderInequality(this);
      return new TypePair(p.typeB, p.typeA);
    }

    // other types
    switch (this.testForEquality(that)) {
      case TRUE:
        return new TypePair(NO_TYPE, NO_TYPE);

      case FALSE:
      case UNKNOWN:
        return new TypePair(this, that);
    }

    // switch case is exhaustive
    throw new IllegalStateException();
  }