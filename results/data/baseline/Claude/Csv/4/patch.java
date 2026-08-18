public Map<String, Integer> getHeaderMap() {
    return Collections.unmodifiableMap(new LinkedHashMap<String, Integer>(this.headerMap));
}