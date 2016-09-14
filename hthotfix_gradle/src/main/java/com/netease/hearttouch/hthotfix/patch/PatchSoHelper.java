/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.patch;

import com.netease.hearttouch.hthotfix.Constants;
import com.netease.hearttouch.hthotfix.FileUtil;
import com.netease.hearttouch.hthotfix.SopathVisitor;
import org.apache.commons.codec.digest.DigestUtils;
import org.gradle.api.Project;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lht on 16/7/25.
 */
public class PatchSoHelper {


    public static boolean isSoExist = false;

    /**
     * 遍历so文件生成hash
     * @param project
     * @param soDir
     */
    public static void hashSoFiles(final Project project, ArrayList<String> soDir) {
        final HashMap<String, String> hashMap = new HashMap<>();
        final String projectDir = project.getProjectDir().toString();

        try {
            for (String dirPath: soDir) {
                File dir = new File(dirPath);
                if (!dir.exists()) continue;
                Files.walkFileTree(dir.toPath(), new SopathVisitor() {
                    @Override
                    protected void visitSo(Path path, byte[] bytecode) {
                        String soFilePath = path.toString();
                        soFilePath = soFilePath.replace(projectDir, "");
                        String sha1Hex = DigestUtils.shaHex(bytecode);
                        hashMap.put(soFilePath,sha1Hex);
                    }
                });
            }

            if (!hashMap.isEmpty()) {
                isSoExist = true;

                HashFileHelper.generate(hashMap,
                        Constants.getHotfixPath(project, Constants.HOTFIX_SO_HASH),
                        project.getLogger(),
                        "PatchSoHelper generate error");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Project project;
    private boolean onlyARM;

    public PatchSoHelper(Project project, boolean onlyARM) {
        this.project = project;
        this.onlyARM = onlyARM;
    }

    //<CPU architecture name, .so files>
    private HashMap<String, ArrayList<File>> soFileMap;

    /**
     * 收集各CPU架构名对应的待打包so文件
     */
    public int collectSoFiles() {
        HashMap<String, String> hashMap = HashFileHelper.parse(
                Constants.getHotfixPath(project, Constants.HOTFIX_SO_HASH));

        soFileMap = new HashMap<>();

        for (HashMap.Entry<String, String> entry : hashMap.entrySet()) {
            String soPath = project.getProjectDir().toString() + entry.getKey();
            File soFile = new File(soPath);

            if (!soFile.exists()) continue;

            String archName = soFile.getParentFile().getName();
            if (onlyARM && !archName.equals("armeabi")) continue;

            String soHash = entry.getValue();

            try {
                String sha1Hex =  DigestUtils.shaHex(Files.readAllBytes(soFile.toPath()));
                if (!sha1Hex.equals(soHash)) {

                    if (soFileMap.containsKey(archName)) {
                        ArrayList<File> soFileList = soFileMap.get(archName);
                        soFileList.add(soFile);
                    } else {
                        ArrayList<File> soFileList = new ArrayList<>();
                        soFileList.add(soFile);
                        soFileMap.put(archName, soFileList);
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return soFileMap.size();
    }

    /**
     * 整理dex和so文件到armeabi文件夹
     * @param dexPath
     */
    public void getARM(String dexPath) {
        File dexFile = new File(dexPath);
        String archName = "armeabi";

        // ../hotfix/patch/
        String patchDirPath = Constants.getHotfixPath(project,
                Constants.HOTFIX_DIR + Constants.HOTFIX_PATCH_DIR + File.separator);

        makePatchDir(dexFile, patchDirPath, soFileMap.get(archName));
        dexFile.delete();
    }

    /**
     * 整理dex和so文件到各CPU架构文件夹,并返回架构名
     * @param dexPath
     * @return
     */
    public String[] getArchs(String dexPath) {
        File dexFile = new File(dexPath);
        String[] archs = new String[soFileMap.size()];
        int i = 0;

        for (HashMap.Entry<String, ArrayList<File>> entry : soFileMap.entrySet()) {
            String archName = entry.getKey();

            // ../hotfix/armeabi/patch/
            String patchDirPath = Constants.getHotfixPath(project,
                    Constants.HOTFIX_DIR + archName + File.separator + Constants.HOTFIX_PATCH_DIR + File.separator);

            makePatchDir(dexFile, patchDirPath, entry.getValue());

            archs[i++] = archName;
        }
        dexFile.delete();

        return archs;
    }

    private void makePatchDir(File dexFile, String patchDirPath, ArrayList<File> soFiles) {
        try {
            File dirFile = new File(patchDirPath + "libs");
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }

            //copy classes.dex to ../hotfix/patch/
            if (dexFile.exists()) {
                InputStream in = new FileInputStream(dexFile);
                OutputStream out = new FileOutputStream(patchDirPath + Constants.HOTFIX_PATCH_DEX_NAME);
                FileUtil.copyFile(in, out);
                in.close();
                out.close();
            }

            //copy .so
            for (File file: soFiles) {
                InputStream soIn = new FileInputStream(file);
                OutputStream soOut = new FileOutputStream(
                        new File(dirFile.getAbsoluteFile(), file.getName()));
                FileUtil.copyFile(soIn, soOut);
                soIn.close();
                soOut.close();
            }
        } catch (Exception e) {
            project.getLogger().error(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
