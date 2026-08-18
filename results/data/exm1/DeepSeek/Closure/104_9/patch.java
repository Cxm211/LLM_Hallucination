JSType meet(JSType that) {
    UnionTypeBuilder builder = new UnionTypeBuilder(registry);
    if (that instanceof UnionType) {
      for (JSType a : this.alternates) {
        for (JSType b : ((UnionType) that).alternates) {
          JSType meet = a.meet(b);
          if (meet != null && !meet.isNoType()) {
            builder.addAlternate(meet);
          }
        }
      }
    } else {
      for (JSType alternate : alternates) {
        if (alternate.isSubtype(that)) {
          builder.addAlternate(alternate);
        }
      }
      if (that.isSubtype(this)) {
        builder.addAlternate(that);
      }
    }
    JSType result = builder.build();
    if (result != null) {
      return result;
    } else if (this.isObject() && that.isObject()) {
      return getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    } else {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
  }