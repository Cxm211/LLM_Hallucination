public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    for (String id : ALL_IDS) {
        if (text.startsWith(id, position)) {
            bucket.setZone(DateTimeZone.forID(id));
            return position + id.length();
        }
    }
    return ~position;
}