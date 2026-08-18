protected void _format(TimeZone tz, Locale loc, Date date,
            StringBuffer buffer)
    {
        Calendar cal = _getCalendar(tz);
        cal.setTime(date);
        // [databind#2167]: handle range beyond [1, 9999]
        int year = cal.get(Calendar.YEAR);

        // Handle BCE (aka BC) years properly: ISO-8601 uses year 0000 for 1 BCE,
        // and negative years for earlier dates. GregorianCalendar indicates BCE via ERA.
        if (cal instanceof GregorianCalendar) {
            if (cal.get(Calendar.ERA) == GregorianCalendar.BC) {
                year = 1 - year; // 1 BCE -> 0, 2 BCE -> -1, etc
            }
        }

        if (year < 0) {
            buffer.append('-');
            pad4(buffer, -year);
        } else {
            pad4(buffer, year);
        }
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
        buffer.append('.');
        pad3(buffer, cal.get(Calendar.MILLISECOND));

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
            // 24-Jun-2017, tatu: While `Z` would be conveniently short, older specs
            //   mandate use of full `+0000`
//            formatted.append('Z');
            if( _tzSerializedWithColon ) {
                buffer.append("+00:00");
            }
            else {
                buffer.append("+0000");
            }
        }
    }