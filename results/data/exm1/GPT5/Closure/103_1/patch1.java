
    private Set<JSType> getTypesToSkipForTypeNonUnion(JSType type) {
      Set<JSType> types = Sets.newHashSet();
      JSType skipType = type;
      while (skipType != null) {
        types.add(skipType);

        ObjectType objSkipType = skipType.toObjectType();
        if (objSkipType != null) {
          // Also skip related interface types in the hierarchy.
          FunctionType ctor = objSkipType.getConstructor();
          if (ctor != null) {
            if (ctor.isInterface()) {
              for (ObjectType ext : ctor.getExtendedInterfaces()) {
                if (ext != null) {
                  types.add(ext);
                  ObjectType p = ext.getImplicitPrototype();
                  while (p != null && types.add(p)) {
                    p = p.getImplicitPrototype();
                  }
                }
              }
            } else {
              for (ObjectType impl : ctor.getImplementedInterfaces()) {
                if (impl != null) {
                  types.add(impl);
                  ObjectType p = impl.getImplicitPrototype();
                  while (p != null && types.add(p)) {
                    p = p.getImplicitPrototype();
                  }
                }
              }
            }
          }

          skipType = objSkipType.getImplicitPrototype();
        } else {
          break;
        }
      }
      return types;
    }