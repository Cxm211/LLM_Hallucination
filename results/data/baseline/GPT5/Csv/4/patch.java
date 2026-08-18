public Map<String, Integer> getHeaderMap() {
    if (this.headerMap == null) {
        return new LinkedHashMap<String, Integer>();
    }
    return new LinkedHashMap<String, Integer>(this.headerMap);
}