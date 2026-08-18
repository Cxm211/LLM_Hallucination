  JSType meet(JSType that) {
    UnionTypeBuilder builder = new UnionTypeBuilder(registry);

    // If the other type is a union, compute pairwise meets between all
    // alternates. Otherwise, meet each of our alternates with the other type.
    if (that instanceof UnionType) {
      for (JSType alternate : alternates) {
        for (JSType otherAlternate : ((UnionType) that).alternates) {
          JSType m = alternate.meet(otherAlternate);
          if (m != null) {
            builder.addAlternate(m);
          }
        }
      }
    } else {
      for (JSType alternate : alternates) {
        JSType m = alternate.meet(that);
        if (m != null) {
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