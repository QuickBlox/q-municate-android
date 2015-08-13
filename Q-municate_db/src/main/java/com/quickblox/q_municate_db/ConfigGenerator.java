package com.quickblox.q_municate_db;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;
import com.quickblox.q_municate_db.helpers.DataHelper;

import java.io.File;

public class ConfigGenerator extends OrmLiteConfigUtil {

    public static void main(String[] args) throws Exception {
        File rawFolder = new File("Q-municate_db/src/main/res/raw");
        File configFile = new File(rawFolder, "orm.properties");
        rawFolder.mkdirs();
        configFile.createNewFile();
        writeConfigFile(configFile, DataHelper.TABLES);
    }
}