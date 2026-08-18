// ===== FIXED com.google.javascript.rhino.jstype.JSTypeRegistry :: canPropertyBeDefined(JSType, String) [lines 690-708] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-165-fixed/src/com/google/javascript/rhino/jstype/JSTypeRegistry.java =====
  public boolean canPropertyBeDefined(JSType type, String propertyName) {
    if (typesIndexedByProperty.containsKey(propertyName)) {
      for (JSType alt :
               typesIndexedByProperty.get(propertyName).getAlternates()) {
        JSType greatestSubtype = alt.getGreatestSubtype(type);
        if (!greatestSubtype.isEmptyType()) {
          // We've found a type with this property. Now we just have to make
          // sure it's not a type used for internal bookkeeping.
          RecordType maybeRecordType = greatestSubtype.toMaybeRecordType();
          if (maybeRecordType != null && maybeRecordType.isSynthetic()) {
            continue;
          }

          return true;
        }
      }
    }
    return false;
  }

// ===== FIXED com.google.javascript.rhino.jstype.RecordType :: RecordType [lines 71-73] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-165-fixed/src/com/google/javascript/rhino/jstype/RecordType.java =====
  RecordType(JSTypeRegistry registry, Map<String, RecordProperty> properties) {
    this(registry, properties, true);
  }

// ===== FIXED com.google.javascript.rhino.jstype.RecordType :: getGreatestSubtypeHelper(JSType) [lines 162-222] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-165-fixed/src/com/google/javascript/rhino/jstype/RecordType.java =====
  JSType getGreatestSubtypeHelper(JSType that) {
    if (that.isRecordType()) {
      RecordType thatRecord = that.toMaybeRecordType();
      RecordTypeBuilder builder = new RecordTypeBuilder(registry);
      builder.setSynthesized(true);

      // The greatest subtype consists of those *unique* properties of both
      // record types. If any property conflicts, then the NO_TYPE type
      // is returned.
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

// ===== FIXED com.google.javascript.rhino.jstype.RecordTypeBuilder :: build() [lines 89-97] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-165-fixed/src/com/google/javascript/rhino/jstype/RecordTypeBuilder.java =====
  public JSType build() {
     // If we have an empty record, simply return the object type.
    if (isEmpty) {
       return registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    }

    return new RecordType(
        registry, Collections.unmodifiableMap(properties), isDeclared);
  }
