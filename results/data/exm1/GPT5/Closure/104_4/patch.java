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
      boolean thisAllObjects = true;
      for (JSType alt : alternates) {
        if (!alt.isObject()) {
          thisAllObjects = false;
          break;
        }
      }
      boolean thatAllObjects;
      if (that instanceof UnionType) {
        thatAllObjects = true;
        for (JSType alt : ((UnionType) that).alternates) {
          if (!alt.isObject()) {
            thatAllObjects = false;
            break;
          }
        }
      } else {
        thatAllObjects = that.isObject();
      }

      if (thisAllObjects && thatAllObjects) {
        return getNativeType(JSTypeNative.NO_OBJECT_TYPE);
      } else {
        return getNativeType(JSTypeNative.NO_TYPE);
      }
    }
  }