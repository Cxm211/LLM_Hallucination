  private boolean hasExceptionHandler(Node cfgNode) {
    if (cfgNode == null) {
      return false;
    }
    try {
      java.lang.reflect.Method m = cfgNode.getClass().getMethod("hasExceptionHandler");
      Object r = m.invoke(cfgNode);
      if (r instanceof Boolean) {
        return ((Boolean) r).booleanValue();
      }
    } catch (Exception ignore) {
    }
    try {
      java.lang.reflect.Method m = cfgNode.getClass().getMethod("hasExceptionHandlers");
      Object r = m.invoke(cfgNode);
      if (r instanceof Boolean) {
        return ((Boolean) r).booleanValue();
      }
    } catch (Exception ignore) {
    }
    try {
      java.lang.reflect.Method m = cfgNode.getClass().getMethod("hasCatchHandler");
      Object r = m.invoke(cfgNode);
      if (r instanceof Boolean) {
        return ((Boolean) r).booleanValue();
      }
    } catch (Exception ignore) {
    }
    try {
      java.lang.reflect.Method m = cfgNode.getClass().getMethod("hasCatchHandlers");
      Object r = m.invoke(cfgNode);
      if (r instanceof Boolean) {
        return ((Boolean) r).booleanValue();
      }
    } catch (Exception ignore) {
    }
    String name = cfgNode.getClass().getSimpleName().toLowerCase();
    return name.contains("try") || name.contains("catch") || name.contains("handler") || name.contains("exception");
  }
