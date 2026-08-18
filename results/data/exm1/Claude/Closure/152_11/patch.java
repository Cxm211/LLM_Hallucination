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
      List<FunctionType> resolvedSubTypes = new ArrayList<FunctionType>();
      for (int i = 0; i < subTypes.size(); i++) {
        resolvedSubTypes.add((FunctionType) subTypes.get(i).resolve(t, scope));
      }
      for (int i = 0; i < resolvedSubTypes.size(); i++) {
        subTypes.set(i, resolvedSubTypes.get(i));
      }
    }

    return super.resolveInternal(t, scope);
  }