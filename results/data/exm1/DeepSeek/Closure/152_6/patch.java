  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    JSType resolvedTypeOfThis = safeResolve(typeOfThis, t, scope);
    if (resolvedTypeOfThis instanceof ObjectType) {
      typeOfThis = (ObjectType) resolvedTypeOfThis;
    } else {
      typeOfThis = resolvedTypeOfThis;
    }

    boolean changed = false;
    ImmutableList.Builder<ObjectType> resolvedInterfaces =
        ImmutableList.builder();
    if (implementedInterfaces != null) {
      for (ObjectType iface : implementedInterfaces) {
        JSType resolvedIface = iface.resolve(t, scope);
        if (resolvedIface instanceof ObjectType) {
          resolvedInterfaces.add((ObjectType) resolvedIface);
          changed |= (resolvedIface != iface);
        } else {
          resolvedInterfaces.add(iface);
        }
      }
    }
    if (changed) {
      implementedInterfaces = resolvedInterfaces.build();
    }

    if (subTypes != null) {
      for (int i = 0; i < subTypes.size(); i++) {
        subTypes.set(i, (FunctionType) subTypes.get(i).resolve(t, scope));
      }
    }

    return super.resolveInternal(t, scope);
  }