public boolean canPropertyBeDefined(JSType type, String propertyName) {
    if (typesIndexedByProperty.containsKey(propertyName)) {
      for (JSType alt :
               typesIndexedByProperty.get(propertyName).getAlternates()) {
        JSType greatestSubtype = alt.getGreatestSubtype(type);
        if (!greatestSubtype.isEmptyType()) {
          // We've found a type with this property. Now we just have to make
          // sure it's not a type used for internal bookkeeping.
          if (greatestSubtype.isEquivalentTo(
                  registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE)) ||
              greatestSubtype.isEquivalentTo(
                  registry.getNativeType(JSTypeNative.NO_TYPE))) {
            return false;
          }
          return true;
        }
      }
    }
    return false;
  }