package com.android.internal.telephony;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import com.android.internal.util.XmlUtils;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;

/**
 * When there is no NITZ reports, get capital city timezone by 
 * country iso for multi timezones country.
 */
public class CapitalTimeZoneUtil {
    private static ArrayList<Map<String,TimeZone>> timezoneList = null;
    static final String TAG = "CapitalTimeZoneUtil";
    static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    /**
     * Read XML and set the default time zones to timezoneList
     */
    private static void initList(){
        timezoneList = new ArrayList<Map<String,TimeZone>>();
        Resources r = Resources.getSystem();
        XmlResourceParser parser = r.getXml(com.android.internal.R.xml.capital_city_time_zone);
        if(parser != null){
            try {
                XmlUtils.beginDocument(parser, "timezones");
                while (true) {
                    XmlUtils.nextElement(parser);
                    String element = parser.getName();
                    if (element == null || !(element.equals("timezone"))) {
                        break;
                    }
                    String code = parser.getAttributeValue(null, "code");
                    if (parser.next() == XmlPullParser.TEXT) {
                        String zoneIdString = parser.getText();
                        TimeZone zone = TimeZone.getTimeZone(zoneIdString);
                        Map<String,TimeZone> map = new HashMap<String,TimeZone>();
                        map.put(code, zone);
                        timezoneList.add(map);
                    }
                }
                if(DEBUG && timezoneList!=null) Log.d(TAG,"Initializing default time zone list size = " +timezoneList.size());
            } catch (XmlPullParserException e) {
                if(DEBUG) Log.e(TAG,"Got xml parser exception while initializing default time zones.");
            } catch (IOException e) {
                if(DEBUG) Log.e(TAG,"Got IOException while initializing default time zones.");
            } finally {
                parser.close();
            }
        } else {
            if(DEBUG) Log.e(TAG, "Initializing default time zones failed, cannot parser xml.");
        }
    }

    /**
     * Get the default time zone where capital city uses by country ISO code.
     * @param iso country ISO code
     * @return TimeZone default time zone
     */
    public static TimeZone getDefaultTimezoneByIso(String iso){
        //if the list is null initialize it
        if(timezoneList==null){
            try {
            initList();
            } catch (NotFoundException e){
                if(DEBUG) Log.e(TAG, "Initializing default time zones failed, cannot find xml.");
            }
        }
        if(timezoneList==null){
            if(DEBUG) Log.e(TAG, "Initializing default time zones failed, timezonelist is null.");
            return null;
        }

        //search the list and get time zone by country iso
        for(int i=0; i<timezoneList.size(); i++){
            Map<String,TimeZone> map = timezoneList.get(i);
            TimeZone tz= map.get(iso);
            if (tz != null){
                if(DEBUG) Log.d(TAG, "Get default time zone for country code: " + iso + ", time zone: " + tz.toString());
                return map.get(iso);
            }
        }
        return null;
    }
}
