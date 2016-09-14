/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

import org.gradle.api.Project;

import java.io.File;

/**
 * Created by zw on 16/6/17.
 */
public class Constants {


    public static final String HOTFIX_DIR = File.separator + "hthotfix" + File.separator;

    public static final String HOTFIX_INTERMEDIATES_DIR = HOTFIX_DIR + "intermediates" + File.separator;

    /***
     * 补丁包文件名称
     */
    public static final String HOTFIX_PATCH_JAR_NAME = "patch";

    /***
     * 补丁包jar路径
     */
    public static final String HOTFIX_PATCH_JAR = HOTFIX_INTERMEDIATES_DIR + HOTFIX_PATCH_JAR_NAME + ".jar";

    /**
     * 补丁包dex名
     */
    public static final String HOTFIX_PATCH_DEX_NAME = "classes.dex";

    /**
     * 补丁包dex路径
     */
    public static final String HOTFIX_PATCH_DEX = HOTFIX_DIR + HOTFIX_PATCH_DEX_NAME;

    /**
     * 补丁包apk名
     */
    public static final String HOTFIX_PATCH_UNSIGN_NAME = "unsigned_patch.apk";

    /**
     * 补丁包apk名,已签名
     */
    public static final String HOTFIX_PATCH_SIGNED_NAME = "patch.apk";

    /**
     * 通用补丁包路径
     */
    public static final String HOTFIX_PATCH_DIR = "patch";

    /***
     * 混淆文件
     */
    public static final String HOTFIX_MAPPING_TXT = HOTFIX_INTERMEDIATES_DIR + "mapping.txt";

    /**
     * 需要打入补丁的class文件路径
     */
    public static final String HOTFIX_CLASSES = HOTFIX_INTERMEDIATES_DIR + "classes" + File.separator;

    /**
     * 所有classes文件的hash值
     */
    public static final String HOTFIX_CLASSES_HASH = HOTFIX_INTERMEDIATES_DIR + "classes.hash";

    /**
     * 所有so文件的hash值
     */
    public static final String HOTFIX_SO_HASH = HOTFIX_INTERMEDIATES_DIR + "so.hash";

    public  static String getHotfixPath(Project project, String subPath){
        return project.getRootDir()+subPath;
    }
}
