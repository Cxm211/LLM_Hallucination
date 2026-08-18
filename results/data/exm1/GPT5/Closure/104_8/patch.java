// buggy code
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
      // If both sides are (or contain) object types, return NO_OBJECT_TYPE.
      boolean thisHasObject = this.isObject();
      if (!thisHasObject && this instanceof UnionType) {
        for (JSType alt : ((UnionType) this).alternates) {
          if (alt.isObject()) { thisHasObject = true; break; }
        }
      }
      boolean thatHasObject = that.isObject();
      if (!thatHasObject && that instanceof UnionType) {
        for (JSType alt : ((UnionType) that).alternates) {
          if (alt.isObject()) { thatHasObject = true; break; }
        }
      }
      if (thisHasObject && thatHasObject) {
        return getNativeType(JSTypeNative.NO_OBJECT_TYPE);
      } else {
        return getNativeType(JSTypeNative.NO_TYPE);
      }
    }
  }