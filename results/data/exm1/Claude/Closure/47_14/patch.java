private OriginalMapping getOriginalMappingForEntry(Entry entry) {
    if (entry.getSourceFileId() == UNMAPPED) {
      return null;
    } else {
      Builder x = OriginalMapping.newBuilder()
        .setOriginalFile(sources[entry.getSourceFileId()])
        .setLineNumber(entry.getSourceLine() + 1)
        .setColumnPosition(entry.getSourceColumn());
      if (entry.getNameId() != UNMAPPED) {
        x.setIdentifier(names[entry.getNameId()]);
      }
      return x.build();
    }
  }