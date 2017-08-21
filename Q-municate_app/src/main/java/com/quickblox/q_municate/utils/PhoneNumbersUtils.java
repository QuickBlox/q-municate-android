package com.quickblox.q_municate.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

public class PhoneNumbersUtils {

    public static boolean isPhoneNumberValid(Context context, String phoneNumberString) {
        phoneNumberString = phoneNumberString.replaceAll("[^0-9+]", "");
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();

        if (!phoneNumberString.startsWith("+") && phoneNumberString.length() == 10) {
            phoneNumberString = String.valueOf(phoneNumberUtil.getCountryCodeForRegion(getCountryLatterCodeFromSim(context)) + String.valueOf(Long.parseLong(phoneNumberString)));
        }


        if (!phoneNumberString.startsWith("+")) {
            phoneNumberString = "+" + phoneNumberString;
        }

        try {
            phoneNumber = phoneNumberUtil.parse(phoneNumberString, getCountryLatterCodeFromSim(context));
        } catch (NumberParseException e) {
            //ignore
        }

        return phoneNumber != null && phoneNumberUtil.isValidNumber(phoneNumber);
    }

    public static String getCorrectPhoneNumber(Context context, String phoneNumberString) {
        phoneNumberString = phoneNumberString.replaceAll("[^0-9+]", "");
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

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

            return telephonyManager.getNetworkCountryIso().toUpperCase();
        }

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
}
