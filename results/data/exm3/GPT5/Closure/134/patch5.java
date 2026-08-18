private FunctionType findOverriddenFunction(
        ObjectType ownerType, String propName) {
      // First, check to see if the property is implemented on a superclass
      // by walking the implicit prototype chain.
      ObjectType current = ownerType.getImplicitPrototype();
      while (current != null) {
        JSType propTypeOnSuper = current.getPropertyType(propName);
        if (propTypeOnSuper instanceof FunctionType) {
          return (FunctionType) propTypeOnSuper;
        }
        current = current.getImplicitPrototype();
      }

      // If it's not on a superclass, then check implemented interfaces
      // (and their extended interfaces).
      FunctionType ctor = ownerType.getConstructor();
      if (ctor != null) {
        List<ObjectType> interfaces = ctor.getImplementedInterfaces();
        if (interfaces != null && !interfaces.isEmpty()) {
          java.util.Set<ObjectType> visited = new java.util.HashSet<ObjectType>();
          java.util.ArrayDeque<ObjectType> stack = new java.util.ArrayDeque<ObjectType>(interfaces);
          while (!stack.isEmpty()) {
            ObjectType iface = stack.pop();
            if (!visited.add(iface)) {
              continue;
            }
            JSType propTypeOnIface = iface.getPropertyType(propName);
            if (propTypeOnIface instanceof FunctionType) {
              return (FunctionType) propTypeOnIface;
            }
            // Walk up the interface's own prototype chain (extended interfaces).
            ObjectType ip = iface.getImplicitPrototype();
            if (ip != null) {
              stack.push(ip);
            }
            // Also consider any interfaces that this interface might itself implement/extend
            // via its constructor, if available.
            FunctionType ifaceCtor = iface.getConstructor();
            if (ifaceCtor != null) {
              List<ObjectType> superIfaces = ifaceCtor.getImplementedInterfaces();
              if (superIfaces != null && !superIfaces.isEmpty()) {
                for (ObjectType si : superIfaces) {
                  if (!visited.contains(si)) {
                    stack.add(si);
                  }
                }
              }
            }
          }
        }
      }

      return null;
    }