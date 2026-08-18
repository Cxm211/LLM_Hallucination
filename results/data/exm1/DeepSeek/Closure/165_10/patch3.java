  JSType getGreatestSubtypeHelper(JSType that) {
    if (that.isRecordType()) {
      RecordType thatRecord = that.toMaybeRecordType();
      RecordTypeBuilder builder = new RecordTypeBuilder(registry);

      for (String property : properties.keySet()) {
        if (thatRecord.hasProperty(property) &&
            !thatRecord.getPropertyType(property).isEquivalentTo(
                getPropertyType(property))) {
          return registry.getNativeObjectType(JSTypeNative.NO_TYPE);
        }

        builder.addProperty(property, getPropertyType(property),
            getPropertyNode(property));
      }

      for (String property : thatRecord.properties.keySet()) {
        if (!hasProperty(property)) {
          builder.addProperty(property, thatRecord.getPropertyType(property),
              thatRecord.getPropertyNode(property));
        }
      }

      return builder.build();
    }

    JSType greatestSubtype = registry.getNativeType(
        JSTypeNative.NO_OBJECT_TYPE);
    JSType thatRestrictedToObj =
        registry.getNativeType(JSTypeNative.OBJECT_TYPE)
        .getGreatestSubtype(that);
    if (!thatRestrictedToObj.isEmptyType()) {
      for (Map.Entry<String, JSType> entry : properties.entrySet()) {
        String propName = entry.getKey();
        JSType propType = entry.getValue();
        UnionTypeBuilder builder = new UnionTypeBuilder(registry);
        for (ObjectType alt :
                 registry.getEachReferenceTypeWithProperty(propName)) {
          JSType altPropType = alt.getPropertyType(propName);
          if (altPropType != null && !alt.isEquivalentTo(this) &&
              alt.isSubtype(that) &&
              (propType.isUnknownType() || altPropType.isUnknownType() ||
                  altPropType.isEquivalentTo(propType))) {
            builder.addAlternate(alt);
          }
        }
        greatestSubtype = greatestSubtype.getGreatestSubtype(builder.build());
      }
    }
    return greatestSubtype;
  }