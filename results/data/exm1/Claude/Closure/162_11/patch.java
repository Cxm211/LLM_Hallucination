public Iterator<Var> getVars() {
  return new Iterator<Var>() {
    final Iterator<Var> iterator = vars.values().iterator();
    
    public boolean hasNext() {
      return iterator.hasNext();
    }
    
    public Var next() {
      return iterator.next();
    }
    
    public void remove() {
      throw new UnsupportedOperationException();
    }
  };
}