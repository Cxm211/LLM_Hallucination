    private Set<JSType> getTypesToSkipForTypeNonUnion(JSType type) {
      Set<JSType> types = Sets.newHashSet();
      JSType skipType = type;
      while (skipType != null) {
        if (types.contains(skipType)) {
          break;
        }
        types.add(skipType);

        ObjectType objSkipType = skipType.toObjectType();
        if (objSkipType != null) {
          skipType = objSkipType.getImplicitPrototype();
        } else {
          break;
        }
      }
      return types;
    }