public Map<String, Integer> getHeaderMap() {
        final Map<String, Integer> map = this.headerMap;
        return map == null ? null : new LinkedHashMap<String, Integer>(map);
    }