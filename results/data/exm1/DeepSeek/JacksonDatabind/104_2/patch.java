    protected void _format(TimeZone tz, Locale loc, Date date,
            StringBuffer buffer)
    {
        Calendar cal = _getCalendar(tz);
        cal.setTime(date);
        final int year = cal.get(Calendar.YEAR);

        if (year < 1 || year > 9999) {
            if (year <= 0) {
                buffer.append('-');
                pad4(buffer, -year + 1);
            } else {
                buffer.append('+');
                buffer.append(String.valueOf(year));
            }
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
            if( _tzSerializedWithColon ) {
                buffer.append("+00:00");
            }
            else {
                buffer.append("+0000");
            }
        }
    }