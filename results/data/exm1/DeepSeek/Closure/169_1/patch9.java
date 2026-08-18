  boolean checkRecordEquivalenceHelper(
      RecordType otherRecord, boolean tolerateUnknowns) {
    Set<String> keySet = properties.keySet();
    Map<String, JSType> otherProps = otherRecord.properties;
    if (!otherProps.keySet().equals(keySet)) {
      return false;
    }
    for (String key : keySet) {
      if (!otherProps.get(key).checkEquivalenceHelper(
              properties.get(key), tolerateUnknowns)) {
        return false;
      }
    }
    return true;
  }