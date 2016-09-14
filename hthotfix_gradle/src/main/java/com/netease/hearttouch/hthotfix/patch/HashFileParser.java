/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.patch;

import com.netease.hearttouch.hthotfix.Constants;
import okio.BufferedSource;
import okio.Okio;
import org.apache.commons.codec.digest.DigestUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * Created by zw on 16/5/26.
 */
public class HashFileParser {

    private HashMap<String,String> hashMap;
    public HashFileParser(Project project){
        hashMap= HashFileHelper.parse(
                Constants.getHotfixPath(project, Constants.HOTFIX_CLASSES_HASH));
    }

    public boolean isChanged(String className,byte[] classByte) throws  IOException{
            String sha1Hex =  DigestUtils.shaHex(classByte);
            String oldSha1Hex = hashMap.get(className);
            return !sha1Hex.equals(oldSha1Hex);
    }
}
