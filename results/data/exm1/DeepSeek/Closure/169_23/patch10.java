  JSType getGreatestSubtypeHelper(JSType that) {
    if (that.isRecordType()) {
      RecordType thatRecord = that.toMaybeRecordType();
      RecordTypeBuilder builder = new RecordTypeBuilder(registry);
      builder.setSynthesized(true);

      // For each property of this record type, compute the greatest subtype
      // with the corresponding property of that record type, if it exists.
      for (String property : properties.keySet()) {
        JSType thisPropType = getPropertyType(property);
        if (thatRecord.hasProperty(property)) {
          JSType thatPropType = thatRecord.getPropertyType(property);
          JSType mergedPropType = thisPropType.getGreatestSubtype(thatPropType);
          if (mergedPropType.isEmptyType()) {
            return registry.getNativeObjectType(JSTypeNative.NO_TYPE);
          }
          builder.addProperty(property, mergedPropType, getPropertyNode(property));
        } else {
          builder.addProperty(property, thisPropType, getPropertyNode(property));
        }
      }

      // Add properties from that record that are not in this record.
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
      // In this branch, the other type is some object type. We find
      // the greatest subtype with the following algorithm:
      // 1) For each property "x" of this record type, take the union
      //    of all classes with a property "x" with a compatible property type.
      //    and which are a subtype of {@code that}.
      // 2) Take the intersection of all of these unions.
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
        greatestSubtype = greatestSubtype.getLeastSupertype(builder.build());
      }
    }
    return greatestSubtype;
  }