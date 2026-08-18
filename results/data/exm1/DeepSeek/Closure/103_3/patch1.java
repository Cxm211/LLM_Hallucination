    private Set<JSType> getTypesToSkipForTypeNonUnion(JSType type) {
      Set<JSType> types = Sets.newHashSet();
      if (type.isUnionType()) {
        for (JSType alternate : type.getAlternates()) {
          types.addAll(getTypesToSkipForTypeNonUnion(alternate));
        }
      } else {
        JSType skipType = type;
        while (skipType != null) {
          types.add(skipType);
          ObjectType objSkipType = skipType.toObjectType();
          if (objSkipType != null) {
            skipType = objSkipType.getImplicitPrototype();
          } else {
            break;
          }
        }
      }
      return types;
    }