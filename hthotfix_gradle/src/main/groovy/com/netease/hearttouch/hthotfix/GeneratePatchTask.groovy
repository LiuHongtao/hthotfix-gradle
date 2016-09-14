/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix

import com.netease.hearttouch.hthotfix.patch.PatchSoHelper
import org.apache.tools.ant.taskdefs.condition.Os

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask

/**
 * 生成补丁包相关task
 * Created by zw on 16/6/2.
 */
public class GeneratePatchTask extends DefaultTask {


    String getOutDexPath() {
        return outDexPath
    }

    void setOutDexPath(String outDexPath) {
        this.outDexPath = outDexPath
    }
    private String outDexPath

    private File outDexFile;

    String getInClassDir() {
        return inClassDir
    }

    void setInClassDir(String inClassDir) {
        this.inClassDir = inClassDir
    }
    private String inClassDir

    private String storePassword
    private String keyPassword
    private String keyAlias
    private File storeFile

    String getKeyAlias() {
        return keyAlias
    }

    void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias
    }

    String getStorePassword() {
        return storePassword
    }

    void setStorePassword(String storePassword) {
        this.storePassword = storePassword
    }

    String getKeyPassword() {
        return keyPassword
    }

    void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword
    }

    String getStoreFile() {
        return storeFile
    }

    void setStoreFile(File storeFile) {
        this.storeFile = storeFile
    }

    private boolean soPatch;
    private boolean onlyARM;

    boolean getSoPatch() {
        return soPatch
    }

    void setSoPatch(boolean soPatch) {
        this.soPatch = soPatch
    }

    boolean getOnlyARM() {
        return onlyARM
    }

    void setOnlyARM(boolean onlyARM) {
        this.onlyARM = onlyARM
    }

    @TaskAction
    def dexPatch(){
        File  inClassFile = new File(inClassDir)
        outDexFile = new File(outDexPath)
        if(outDexFile.exists()){
            outDexFile.delete()
        }
        if(!inClassFile.exists()){
            inClassFile.mkdir()
        }
        def project = getProject()
        dex(project,inClassFile)

        PatchSoHelper soHelper = new PatchSoHelper(project, onlyARM)

        def soSize = 0
        if (soPatch & PatchSoHelper.isSoExist) {
            soSize = soHelper.collectSoFiles()
        }

        // 有so文件需要更新
        if (soSize > 0) {
            if (onlyARM) {
                soHelper.getARM(outDexPath)

                def hotfixDir = Constants.getHotfixPath(project, Constants.HOTFIX_DIR)
                def patchDirPath = "${hotfixDir}${Constants.HOTFIX_PATCH_DIR}"

                //patch文件夹压缩为apk
                FileUtil.doZip(patchDirPath,
                        "${hotfixDir}${Constants.HOTFIX_PATCH_UNSIGN_NAME}")

                //签名apk
                sign("${hotfixDir}${Constants.HOTFIX_PATCH_UNSIGN_NAME}",
                        "${hotfixDir}${Constants.HOTFIX_PATCH_SIGNED_NAME}")
            }
            else {
                makeZip(project, soHelper.getArchs(outDexPath))
            }
        }
        else {
            def hotfixDir = Constants.getHotfixPath(project, Constants.HOTFIX_DIR)

            //新建patch文件夹
            def patchDir = new File(hotfixDir + Constants.HOTFIX_PATCH_DIR)
            if (!patchDir.exists()) {
                patchDir.mkdirs()
            }
            def patchDirPath = "${patchDir.getAbsolutePath()}${File.separator}"

            //将dex文件移动到patch文件夹
            FileUtil.moveFile(outDexPath,
                    "${patchDirPath}${Constants.HOTFIX_PATCH_DEX_NAME}")

            //patch文件夹压缩为apk
            FileUtil.doZip(patchDirPath,
                    "${hotfixDir}${Constants.HOTFIX_PATCH_UNSIGN_NAME}")

            //签名apk
            sign("${hotfixDir}${Constants.HOTFIX_PATCH_UNSIGN_NAME}",
                    "${hotfixDir}${Constants.HOTFIX_PATCH_SIGNED_NAME}")
        }
    }

    /**
     * 根据不同的cpu架构打包
     * @param project
     * @param archsName
     */
    void makeZip(Project project, String[] archsName) {
        archsName.each {archName->
            // ../hotfix/armeabi/
            def archDir = Constants.getHotfixPath(project,
                    "${Constants.HOTFIX_DIR}${archName}${File.separator}")
            // ../hotfix/armeabi/patch/
            def patchDir = "${archDir}${Constants.HOTFIX_PATCH_DIR}"

            FileUtil.doZip(patchDir,
                    "${archDir}${Constants.HOTFIX_PATCH_UNSIGN_NAME}")

            //签名
            sign("${archDir}${Constants.HOTFIX_PATCH_UNSIGN_NAME}",
                    "${archDir}${Constants.HOTFIX_PATCH_SIGNED_NAME}")
        }
    }

    /**
     * 签名
     * @param dexFile
     * @param signedDexFile 签名后文件路径
     */
    void sign(String dexFile,String signedDexFile){
        if(storePassword!=null&&keyPassword!=null&&keyAlias!=null&&storeFile!=null){
            project.logger.info("sign patch ...")
            def stdout = new ByteArrayOutputStream()

            def sign  = "jarsigner -verbose -keystore ${storeFile.getAbsolutePath()} "+
                        "-storepass ${storePassword} " +
                        "-keypass ${keyPassword} " +
                        "-signedjar ${signedDexFile} ${dexFile} " +
                        " ${keyAlias}"
            def sout = new StringBuilder(), serr = new StringBuilder()

            def    process = sign.execute()
            process.consumeProcessOutput(sout, serr)

            if (process.waitFor() == 0) {
                project.getLogger().info("sign success\t"+sout)
            }else{
                project.getLogger().error("sign failed,\t"+serr)

            }

        }else{
            project.logger.info(" ...")
        }
    }

    /**
     * 生成dex
     * @param project
     * @param inClassDir
     * @return 是否生成成功
     */
    public boolean dex(Project project, File inClassDir) {

        if (inClassDir.listFiles().size()) {
            def sdkDir

            Properties properties = new Properties()
            File localProps = project.rootProject.file("local.properties")
            if (localProps.exists()) {
                properties.load(localProps.newDataInputStream())
                sdkDir = properties.getProperty("sdk.dir")
            } else {
                sdkDir = System.getenv("ANDROID_HOME")
            }
            if (sdkDir) {
                def cmdExt = Os.isFamily(Os.FAMILY_WINDOWS) ? '.bat' : ''
                def stdout = new ByteArrayOutputStream()
                project.exec {
                    commandLine "${sdkDir}${File.separator}build-tools${File.separator}${project.android.buildToolsVersion}${File.separator}dx${cmdExt}",
                            '--dex',
                            "--output=${outDexPath}",
                            "${inClassDir}"
                    standardOutput = stdout
                }
                def error = stdout.toString().trim()
                if (error) {
                    project.getLogger().error( "dex error:" + error);
                }
                else {
                    project.getLogger().error( "dex success" );
                    return true;
                }
            } else {
                throw new InvalidUserDataException('$ANDROID_HOME is not defined')
            }
        }else{
            project.getLogger().error("hotfix has no changed classes");
        }

        return false;
    }
}
