package com.quickblox.q_municate.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

public class PhoneNumbersUtils {

    public static boolean isPhoneNumberValid(Context context, String phoneNumberString) {
        phoneNumberString = phoneNumberString.replaceAll("[^0-9+]", "");
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        Log.d("PhoneNumbersUtils", "current country code " + getCountryLatterCodeFromSim(context));
//        phoneNumber.setCountryCode(phoneNumberUtil.getCountryCodeForRegion(getCountryLatterCodeFromSim(context)));

        if (!phoneNumberString.startsWith("+") && phoneNumberString.length() == 10) {
            phoneNumberString = String.valueOf(phoneNumberUtil.getCountryCodeForRegion(getCountryLatterCodeFromSim(context)) + String.valueOf(Long.parseLong(phoneNumberString)));
        }


        if (!phoneNumberString.startsWith("+")) {
            phoneNumberString = "+" + phoneNumberString;
        }

        Log.v("PhoneNumbersUtils", "number to validation " + phoneNumberString);
        try {
            phoneNumber = phoneNumberUtil.parse(phoneNumberString, getCountryLatterCodeFromSim(context));
        } catch (NumberParseException e) {
            Log.e("PhoneNumbersUtils", "number not valid - " + phoneNumberString);
        }

        return phoneNumber != null && phoneNumberUtil.isValidNumber(phoneNumber);
    }

    public static String getCorrectPhoneNumber(Context context, String phoneNumberString) {
//        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
//        Phonenumber.PhoneNumber phoneNumber = null;
//
//        try {
//            phoneNumber = phoneNumberUtil.parse(phoneNumberString, getCountryLatterCodeFromSim(context));
//        } catch (NumberParseException e) {
//            //ignore
//            Log.v("PhoneNumbersUtils", "number not valid " + phoneNumberString);
//        }
//
//        return (String.valueOf(phoneNumber.getCountryCode()) + phoneNumber.getNationalNumber()).replaceAll("[^0-9]", "");
        phoneNumberString = phoneNumberString.replaceAll("[^0-9+]", "");
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        Log.d("PhoneNumbersUtils", "current country code " + getCountryLatterCodeFromSim(context));
//        phoneNumber.setCountryCode(phoneNumberUtil.getCountryCodeForRegion(getCountryLatterCodeFromSim(context)));

        if (!phoneNumberString.startsWith("+") && phoneNumberString.length() == 10) {
            phoneNumberString = String.valueOf(phoneNumberUtil.getCountryCodeForRegion(getCountryLatterCodeFromSim(context)) + String.valueOf(Long.parseLong(phoneNumberString)));
        }


        if (phoneNumberString.startsWith("+")) {
            phoneNumberString = phoneNumberString.replaceAll("\\+", "");
        }


        return phoneNumberString;
    }

    public static int getCountryCodeFromSim(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        return phoneNumberUtil.getCountryCodeForRegion(telephonyManager.getSimCountryIso());
    }

    public static String getDefoultLocale() {
        return Locale.getDefault().getCountry();
    }

    public static int getDefaultCountryCode() {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        return phoneNumberUtil.getCountryCodeForRegion(getDefoultLocale());
    }

    public static String getCountryLatterCodeFromSim(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (TextUtils.isEmpty(telephonyManager.getSimCountryIso())) {
//            Log.d("PhoneNumbersUtils", "current country code " + telephonyManager.getNetworkCountryIso());

            return telephonyManager.getNetworkCountryIso().toUpperCase();
        }

//        Log.d("PhoneNumbersUtils", "current country code " + telephonyManager.getSimCountryIso());
        return telephonyManager.getSimCountryIso().toUpperCase();
    }

    public static int getCountryCodeFromPhoneNumber(String phoneNumberString) {
        int countryCode = -1;

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = null;

        try {
            phoneNumber = phoneNumberUtil.parse(phoneNumberString, "GB");
            countryCode = phoneNumber.getCountryCode();
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        return countryCode;
    }

    public static String getInternationalPhoneNumber(String phoneNumberString) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = null;

        try {
            phoneNumber = phoneNumberUtil.parse(phoneNumberString, "GB");
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        return String.valueOf(phoneNumber.getCountryCode()) + phoneNumber.getNationalNumber();
    }

//    public static String getCurrentLocale(Context context){
//        Locale locale;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            locale = context.getResources().getConfiguration().getLocales().get(0);
//        } else {
//            locale = context.getResources().getConfiguration().locale;
//        }
//
//        return locale.getCountry();
//    }

}
