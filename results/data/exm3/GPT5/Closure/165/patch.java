public boolean canPropertyBeDefined(JSType type, String propertyName) {
    if (typesIndexedByProperty.containsKey(propertyName)) {
      for (JSType alt :
               typesIndexedByProperty.get(propertyName).getAlternates()) {
        // A property can be defined on 'type' only if there exists a type
        // that actually has this property and is a subtype of 'type'.
        if (alt.isSubtype(type)) {
          return true;
        }
      }
    }
    return false;
  }