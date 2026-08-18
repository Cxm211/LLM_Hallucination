  String toStringHelper(boolean forAnnotations) {
    if (hasReferenceName()) {
      return getReferenceName();
    }

    // When generating annotation strings, prefer a nominal name. If none,
    // fallback to the generic Object rather than structural pretty-printing.
    if (forAnnotations) {
      return "Object";
    }

    if (prettyPrint) {
      // Don't pretty print recursively.
      boolean oldPrettyPrint = prettyPrint;
      prettyPrint = false;

      // Use a tree set so that the properties are sorted.
      Set<String> propertyNames = Sets.newTreeSet();
      for (ObjectType current = this;
           current != null && !current.isNativeObjectType() &&
               propertyNames.size() <= MAX_PRETTY_PRINTED_PROPERTIES;
           current = current.getImplicitPrototype()) {
        propertyNames.addAll(current.getOwnPropertyNames());
      }

      StringBuilder sb = new StringBuilder();
      sb.append("{");

      int i = 0;
      for (String property : propertyNames) {
        if (i > 0) {
          sb.append(", ");
        }

        sb.append(property);
        sb.append(": ");
        sb.append(getPropertyType(property).toString());

        ++i;
        if (i == MAX_PRETTY_PRINTED_PROPERTIES) {
          sb.append(", ...");
          break;
        }
      }

      sb.append("}");

      // Restore previous prettyPrint state.
      prettyPrint = oldPrettyPrint;
      return sb.toString();
    } else {
      return "{...}";
    }
  }