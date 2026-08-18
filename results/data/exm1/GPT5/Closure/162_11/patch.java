public Iterator<Var> getVars() {
    // Return an iterator over a snapshot to avoid ConcurrentModificationException
    return new java.util.ArrayList<Var>(vars.values()).iterator();
  }