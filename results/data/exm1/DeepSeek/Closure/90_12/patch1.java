  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    JSType maybeTypeOfThis = safeResolve(typeOfThis, t, scope);
    if (maybeTypeOfThis instanceof ObjectType) {
      typeOfThis = (ObjectType) maybeTypeOfThis;
    } else if (maybeTypeOfThis instanceof UnionType) {
      // If the resolved type is a union type that only contains object types,
      // we can use the first object type as a representative.
      // This is a minimal fix for the known issue with union types.
      UnionType unionType = (UnionType) maybeTypeOfThis;
      for (JSType alternative : unionType.getAlternatives()) {
        if (alternative instanceof ObjectType) {
          typeOfThis = (ObjectType) alternative;
          break;
        }
      }
    }

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
      for (int i = 0; i < subTypes.size(); i++) {
        subTypes.set(i, (FunctionType) subTypes.get(i).resolve(t, scope));
      }
    }

    return super.resolveInternal(t, scope);
  }