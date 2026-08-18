public Var getVar(String name) {
    Var var = vars.get(name);
    if (var != null) {
      return var;
    } else if (parent != null) { // Recurse up the parent Scope
      // Each function scope has its own special 'arguments' object.
      // Do not resolve 'arguments' from parent scopes.
      if ("arguments".equals(name)) {
        return null;
      }
      return parent.getVar(name);
    } else {
      return null;
    }
  }