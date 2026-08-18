public Iterator<Var> getVars() {
    // Return an iterator over a snapshot to avoid concurrent modification issues
    return new java.util.ArrayList<Var>(vars.values()).iterator();
  }