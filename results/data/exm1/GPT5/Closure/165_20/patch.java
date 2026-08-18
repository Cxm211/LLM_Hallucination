public boolean canPropertyBeDefined(JSType type, String propertyName) {
    if (typesIndexedByProperty.containsKey(propertyName)) {
      for (JSType alt :
               typesIndexedByProperty.get(propertyName).getAlternates()) {
        JSType greatestSubtype = alt.getGreatestSubtype(type);
        if (!greatestSubtype.isEmptyType()) {
          // We've found a type with this property. Now we just have to make
          // sure it's not a type used for internal bookkeeping.
          ObjectType altObj = alt.toObjectType();
          if (altObj == null) {
            continue;
          }
          if (altObj.isFunctionPrototypeType()) {
            continue;
          }
          return true;
        }
      }
    }
    return false;
  }