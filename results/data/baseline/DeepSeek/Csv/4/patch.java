public Map<String, Integer> getHeaderMap() {
    if (this.headerMap == null) {
        return new LinkedHashMap<>();
    }
    return new LinkedHashMap<>(this.headerMap);
}