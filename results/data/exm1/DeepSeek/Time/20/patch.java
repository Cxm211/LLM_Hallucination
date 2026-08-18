public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    String[] sortedIds = ALL_IDS.clone();
    java.util.Arrays.sort(sortedIds, (a, b) -> b.length() - a.length());
    for (String id : sortedIds) {
        if (str.startsWith(id)) {
            bucket.setZone(DateTimeZone.forID(id));
            return position + id.length();
        }
    }
    return ~position;
}