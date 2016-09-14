/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by zw on 16/6/7.
 */
public class HTHotfixExtension {

    private File storeFile;
    private String storePassword;
    private String keyAlias;
    private String keyPassword;

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public File getStoreFile() {
        return storeFile;
    }

    public void setStoreFile(File storeFile) {
        this.storeFile = storeFile;
    }

    public String getStorePassword() {
        return storePassword;
    }

    public void setStorePassword(String storePassword) {
        this.storePassword = storePassword;
    }

    public boolean isGeneratePatch() {
        return generatePatch;
    }

    private boolean scanRef;
    private boolean generatePatch = false;

    public boolean getScanRef() {
        return scanRef;
    }

    public void setScanRef(boolean scanRef) {
        this.scanRef = scanRef;
    }

    public boolean  getGeneratePatch(){
        return generatePatch;
    }

    public void setGeneratePatch(boolean isPatch){
        this.generatePatch = isPatch;
    }

    private ArrayList<String> includePackage ;
    private ArrayList<String> excludeClass;

    public ArrayList<String> getIncludePackage() {
        return includePackage;
    }

    public void setIncludePackage(ArrayList<String> includePackage) {
        this.includePackage = includePackage;
    }

    public ArrayList<String> getExcludeClass() {
        return excludeClass;
    }

    public void setExcludeClass(ArrayList<String> excludeClass) {
        this.excludeClass = excludeClass;
    }

    //added by lht 16/7/25
    private boolean soPatch = true;
    private boolean onlyARM = true;

    public boolean isSoPatch() {
        return soPatch;
    }

    public void setSoPatch(boolean soPatch) {
        this.soPatch = soPatch;
    }

    public boolean isOnlyARM() {
        return onlyARM;
    }

    public void setOnlyARM(boolean onlyARM) {
        this.onlyARM = onlyARM;
    }
}
