    public boolean traverseEdge(NameInfo start, JSModule edge, NameInfo dest) {
      if (start.isReferenced()) {
        JSModule startModule = start.getDeepestCommonModuleRef();
        if (startModule != null &&
            moduleGraph.dependsOn(startModule, edge)) {
          return dest.markReference(startModule);
        } else {
          return dest.markReference(edge);
        }
      }
      return false;
    }

