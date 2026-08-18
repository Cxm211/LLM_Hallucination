public boolean canPropertyBeDefined(JSType type, String propertyName) {
  if (typesIndexedByProperty.containsKey(propertyName)) {
    for (JSType alt :
             typesIndexedByProperty.get(propertyName).getAlternates()) {
      JSType greatestSubtype = alt.getGreatestSubtype(type);
      if (!greatestSubtype.isEmptyType()) {
        if (alt.isFunctionPrototypeType() || alt.isFunctionType()) {
          continue;
        }
        return true;
      }
    }
  }
  return false;
}