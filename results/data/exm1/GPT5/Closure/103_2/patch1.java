
    private Set<JSType> getTypesToSkipForTypeNonUnion(JSType type) {
      Set<JSType> types = Sets.newHashSet();
      Set<ObjectType> seenObjectTypes = Sets.newHashSet();
      JSType skipType = type;
      while (skipType != null) {
        // If we've already seen this JSType, stop to avoid cycles.
        if (!types.add(skipType)) {
          break;
        }

        ObjectType objSkipType = skipType.toObjectType();
        if (objSkipType != null) {
          // If we've already seen this ObjectType in the prototype chain,
          // or the implicit prototype is the object itself, stop to avoid cycles.
          if (!seenObjectTypes.add(objSkipType)) {
            break;
          }
          ObjectType implicitProto = objSkipType.getImplicitPrototype();
          if (implicitProto == null || implicitProto == objSkipType) {
            break;
          }
          skipType = implicitProto;
        } else {
          break;
        }
      }
      return types;
    }