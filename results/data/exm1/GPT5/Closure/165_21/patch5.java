public JSType build() {
     // If we have an empty record, simply return a record type with no properties.
    if (isEmpty) {
       return new RecordType(
           registry, Collections.unmodifiableMap(properties));
    }

    return new RecordType(
        registry, Collections.unmodifiableMap(properties));
  }