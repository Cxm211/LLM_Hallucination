public boolean canPropertyBeDefined(JSType type, String propertyName) {
  if (typesIndexedByProperty.containsKey(propertyName)) {
    for (JSType alt :
             typesIndexedByProperty.get(propertyName).getAlternates()) {
      JSType greatestSubtype = alt.getGreatestSubtype(type);
      if (!greatestSubtype.isEmptyType()) {
        
        
        RecordType maybeRecord = greatestSubtype.toMaybeRecordType();
        if (maybeRecord != null && maybeRecord.isFrozen()) {
          continue;
        }
        return true;
      }
    }
  }
  return false;
}