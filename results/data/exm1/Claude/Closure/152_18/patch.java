JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    typeOfThis = (ObjectType) safeResolve(typeOfThis, t, scope);

    boolean changed = false;
    ImmutableList.Builder<ObjectType> resolvedInterfaces =
        ImmutableList.builder();
    for (ObjectType iface : implementedInterfaces) {
      ObjectType resolvedIface = (ObjectType) iface.resolve(t, scope);
      resolvedInterfaces.add(resolvedIface);
      changed |= (resolvedIface != iface);
    }
    if (changed) {
      implementedInterfaces = resolvedInterfaces.build();
    }

    if (subTypes != null) {
      boolean subTypesChanged = false;
      List<FunctionType> resolvedSubTypes = new ArrayList<FunctionType>(subTypes.size());
      for (FunctionType subType : subTypes) {
        FunctionType resolvedSubType = (FunctionType) subType.resolve(t, scope);
        resolvedSubTypes.add(resolvedSubType);
        subTypesChanged |= (resolvedSubType != subType);
      }
      if (subTypesChanged) {
        subTypes.clear();
        subTypes.addAll(resolvedSubTypes);
      }
    }

    return super.resolveInternal(t, scope);
  }