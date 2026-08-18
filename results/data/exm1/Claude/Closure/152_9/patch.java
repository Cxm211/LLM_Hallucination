JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    // Warning about typeOfThis if it doesn't resolve to an ObjectType
    // is handled further upstream.
    // TODO(nicksantos): Handle this correctly if we have a UnionType.
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
      for (int i = 0; i < subTypes.size(); i++) {
        FunctionType originalSubType = subTypes.get(i);
        FunctionType resolvedSubType = (FunctionType) originalSubType.resolve(t, scope);
        if (resolvedSubType != originalSubType) {
          subTypesChanged = true;
        }
        if (subTypesChanged) {
          subTypes.set(i, resolvedSubType);
        }
      }
    }

    return super.resolveInternal(t, scope);
  }