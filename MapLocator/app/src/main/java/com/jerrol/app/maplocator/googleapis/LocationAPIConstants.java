package com.jerrol.app.maplocator.googleapis;

/**
 * Created by Jerrol-PC on 7/4/2016.
 */
public class LocationAPIConstants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.project.jerrol.samplelocationapi";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    public static final String NO_LOCATION_DATA_PROVIDED = "No location data provided";
    public static final String SERVICE_NOT_AVAILABLE = "Sorry, the service is not available";
    public static final String INVALID_LAT_LONG_USED = "Invalid latitude or longitude used";
    public static final String NO_GEOCODER_AVAILABLE = "No geocoder available";
    public static final String FETCH_ADDRESS = "Fetch address";
    public static final String ADDRESS_FOUND = "Address found";
    public static final String NO_ADDRESS_FOUND = "Sorry, no address found";

    public static final String ADDRESS_FROM_NAMES = "AddressFromName";
}