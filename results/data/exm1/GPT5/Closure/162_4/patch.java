public Iterator<Var> getVars() {
    // Return a snapshot iterator to avoid concurrent modification during iteration.
    return new java.util.ArrayList<Var>(vars.values()).iterator();
  }