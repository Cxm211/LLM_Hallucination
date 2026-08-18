public Iterator<Var> getVars() {
    // Return a snapshot iterator to avoid potential concurrent modification
    return new java.util.ArrayList<Var>(vars.values()).iterator();
  }