public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    String bestId = null;
    for (String id : ALL_IDS) {
        if (str.startsWith(id)) {
            if (bestId == null || id.length() > bestId.length()) {
                bestId = id;
            }
        }
    }
    if (bestId != null) {
        bucket.setZone(DateTimeZone.forID(bestId));
        return position + bestId.length();
    }
    return ~position;
}