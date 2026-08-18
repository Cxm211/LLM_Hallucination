    protected void _format(TimeZone tz, Locale loc, Date date,
            StringBuffer buffer)
    {
        Calendar cal = _getCalendar(tz);
        cal.setTime(date);
        // [databind#2167]: handle range beyond [1, 9999]
        final int year = cal.get(Calendar.YEAR);

        // Assuming GregorianCalendar, special handling needed for BCE (aka BC)
                // 22-Nov-2018, tatu: Handling beyond 4-digits is not well specified wrt ISO-8601, but
                //   it seems that plus prefix IS mandated. Padding is an open question, but since agreeement
                //   for max length would be needed, we ewould need to limit to arbitrary length
                //   like five digits (erroring out if beyond or padding to that as minimum).
                //   Instead, let's just print number out as is and let decoder try to make sense of it.
            pad4(buffer, year);
        buffer.append('-');
        pad2(buffer, cal.get(Calendar.MONTH) + 1);
        buffer.append('-');
        pad2(buffer, cal.get(Calendar.DAY_OF_MONTH));
        buffer.append('T');
        pad2(buffer, cal.get(Calendar.HOUR_OF_DAY));
        buffer.append(':');
        pad2(buffer, cal.get(Calendar.MINUTE));
        buffer.append(':');
        pad2(buffer, cal.get(Calendar.SECOND));
        int ms = cal.get(Calendar.MILLISECOND);
        if (ms != 0) {
            buffer.append('.');
            pad3(buffer, ms);
        }

        int offset = tz.getOffset(cal.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs((offset / (60 * 1000)) / 60);
            int minutes = Math.abs((offset / (60 * 1000)) % 60);
            buffer.append(offset < 0 ? '-' : '+');
            pad2(buffer, hours);
            if( _tzSerializedWithColon ) {
            			buffer.append(':');
            }
            pad2(buffer, minutes);
        } else {
            // For UTC, use 'Z' per ISO-8601
            buffer.append('Z');
        }
    }

    private static void pad4(StringBuffer buffer, int value) {
        int h = value / 100;
        if (h == 0) {
            buffer.append('0').append('0');
        } else {
                pad2(buffer, h);
            value -= (100 * h);
        }
        pad2(buffer, value);
    }