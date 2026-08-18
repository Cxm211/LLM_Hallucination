JSType meet(JSType that) {
    UnionTypeBuilder builder = new UnionTypeBuilder(registry);
    for (JSType alternate : alternates) {
      if (alternate.isSubtype(that)) {
        builder.addAlternate(alternate);
      }
    }

    if (that instanceof UnionType) {
      for (JSType otherAlternate : ((UnionType) that).alternates) {
        if (otherAlternate.isSubtype(this)) {
          builder.addAlternate(otherAlternate);
        }
      }
    } else if (that.isSubtype(this)) {
      builder.addAlternate(that);
    }
    JSType result = builder.build();
    if (result != null) {
      return result;
    } else {
      boolean thisIsObject = this.isObject();
      if (!thisIsObject && this instanceof UnionType) {
        thisIsObject = true;
        for (JSType tAlt : ((UnionType) this).alternates) {
          if (!tAlt.isObject()) {
            thisIsObject = false;
            break;
          }
        }
      }
      boolean thatIsObject = that.isObject();
      if (!thatIsObject && that instanceof UnionType) {
        thatIsObject = true;
        for (JSType tAlt : ((UnionType) that).alternates) {
          if (!tAlt.isObject()) {
            thatIsObject = false;
            break;
          }
        }
      }
      if (thisIsObject && thatIsObject) {
        return getNativeType(JSTypeNative.NO_OBJECT_TYPE);
      } else {
        return getNativeType(JSTypeNative.NO_TYPE);
      }
    }
  }