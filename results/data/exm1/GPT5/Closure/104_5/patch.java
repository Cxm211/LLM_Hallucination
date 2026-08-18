  JSType meet(JSType that) {
    UnionTypeBuilder builder = new UnionTypeBuilder(registry);

    if (that instanceof UnionType) {
      // Compute pairwise meets between all alternates.
      for (JSType a : alternates) {
        for (JSType b : ((UnionType) that).alternates) {
          JSType m = a.meet(b);
          if (m != null && !m.isNoType()) {
            builder.addAlternate(m);
          }
        }
      }
    } else {
      // Meet each alternate with the other type.
      for (JSType a : alternates) {
        JSType m = a.meet(that);
        if (m != null && !m.isNoType()) {
          builder.addAlternate(m);
        }
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