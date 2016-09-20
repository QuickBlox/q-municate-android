package com.quickblox.q_municate.utils;

import java.util.ArrayList;

public class StringUtils {

    public static String createHumanNameFromSystemPermission(String permission){
        String permissionName = permission.replace("android.permission.", "");
        String[] words = permissionName.split("_", 0);
        String newPermissionName = "";
        for(String word : words){
            newPermissionName+= word.substring(0,1) + word.substring(1).toLowerCase() + " ";
        }

        return newPermissionName;
    }

    public static String createCompositeString(ArrayList<String> permissions){
        StringBuilder stringBuilder = new StringBuilder();

        for (String string : permissions){
            stringBuilder.append(createHumanNameFromSystemPermission(string));
            if (permissions.indexOf(string) == permissions.size() -2){
                stringBuilder.append(" and ");
            } else if (permissions.indexOf(string) == permissions.size() -1){
                stringBuilder.append("");
            } else {
                stringBuilder.append(", ");
            }
        }

        return stringBuilder.toString();
    }
}
