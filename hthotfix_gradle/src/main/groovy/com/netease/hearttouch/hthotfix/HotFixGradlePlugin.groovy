/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix
import com.android.build.gradle.AppExtension
import com.android.builder.model.SourceProvider
import com.netease.hearttouch.hthotfix.patch.PatchSoHelper
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.netease.hearttouch.hthotfix.refclass.RefScanInstrument
import com.android.build.gradle.api.ApplicationVariant

class HotFixGradlePlugin implements Plugin<Project> {
    void apply(Project project) {

        def hthotfixExtension= project.extensions.create("hthotfix",HTHotfixExtension)

        def refScanInstrumentContext =new RefScanInstrument(project)

        //生成patch.jar
        def transform = new PatchGeneratorTransform(project,hthotfixExtension,refScanInstrumentContext)
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(transform)

        //加入patch class 相关的reference
        transform = new PatchRefGeneratorTransform(project,hthotfixExtension,refScanInstrumentContext)
        android.registerTransform(transform)

        //生成hash文件
        transform = new HashFileGeneratorTransform(project,hthotfixExtension )
        android.registerTransform(transform)

        project.afterEvaluate{
            project.android.applicationVariants.each { variant->

                if (variant.getBuildType().isMinifyEnabled()) {
                    variant.assemble.doLast {
                        project.copy {
                            from variant.mappingFile
                            into Constants.getHotfixPath(project,Constants.HOTFIX_INTERMEDIATES_DIR)
                        }
                    }
                }

                //遍历so文件并生成so.hash
                if (!hthotfixExtension.getGeneratePatch()) {
                    List<SourceProvider> sourceSets = variant.getSourceSets();
                    SourceProvider defaultSourceProvider = sourceSets.get(0);
                    def jniLibsPath = project.files(defaultSourceProvider.getJniLibsDirectories()).asPath
                    def ndkPath = "${project.getRootDir()}/app/build/intermediates/ndk/${variant.getFlavorName()}/${variant.getBuildType().name}/lib/"

                    ArrayList<String> soDir = new ArrayList<>();
                    soDir.add(ndkPath.toString());
                    soDir.add(jniLibsPath)

                    variant.assemble.doLast {
                        PatchSoHelper.hashSoFiles(project, soDir)
                    }
                }

                def  mappingFile = Constants.getHotfixPath(project,Constants.HOTFIX_MAPPING_TXT)
                def dexFile =Constants.getHotfixPath(project,Constants.HOTFIX_PATCH_DEX)
                def inClasses = Constants.getHotfixPath(project,Constants.HOTFIX_CLASSES)
                //生成patch.apk
                if(hthotfixExtension.getGeneratePatch()) {
                    //删除临时class目录
                    initCleanIntermediatesFile(project)

                    def hotfixPatchTask = initGeneratePatchTask(project,variant,inClasses,dexFile,hthotfixExtension.storeFile,
                            hthotfixExtension.storePassword,hthotfixExtension.keyPassword,hthotfixExtension.keyAlias,
                            hthotfixExtension.soPatch, hthotfixExtension.onlyARM)

                    //在release模式下，混淆后的代码copy到中间目录
                    copyProguardPatchClasses(project,variant,mappingFile,hthotfixExtension)

                    //生成patch.jar
                    def customTransformTask = project.tasks.findByName("transformClassesWithPatchGeneratorFor${variant.name.capitalize()}")
                    if(customTransformTask){
                        def assembleDebug = project.tasks.findByName("assemble${variant.name.capitalize()}")
                        hotfixPatchTask.dependsOn assembleDebug
                    }
                }else{
                }
            }
        }
    }


    private void applyPatchMappingTxt(Project project,ApplicationVariant variant,String mappingPath ){
        File mappingFile = new File(mappingPath)
        if(mappingFile.exists()){
            def manager = variant.variantData.getScope().transformManager;
            def proguardTransform = manager.transforms.find {
                it.getName() == "proguard"
            };
            if (proguardTransform) {
                proguardTransform.configuration.applyMapping = mappingFile
            }
        }else{
            project.logger.error("patch apply mapping  txt,but  file not exist")
        }
    }

    private void initCleanIntermediatesFile(Project project) {
        def cleanTask = project.tasks.findByName("clean")
        if (cleanTask) {
            cleanTask.doLast({
                //临时class文件
                File file = new File(Constants.getHotfixPath(project, Constants.HOTFIX_CLASSES))
                if (file.exists())
                    file.deleteDir()
                //classes.dex
                file = new File(Constants.getHotfixPath(project, Constants.HOTFIX_PATCH_DEX))
                if (file.exists())
                    file.delete();

                file = new File(Constants.getHotfixPath(project, Constants.HOTFIX_DIR));
                file.listFiles().each {subFile->
                    if  (subFile.isDirectory() && !subFile.getName().equals("intermediates")) {
                        subFile.deleteDir()
                    }
                }
            })

        }
    }

    void copyProguardPatchClasses(Project project,ApplicationVariant variant,String mappingPath,HTHotfixExtension extension){
        if (variant.getBuildType().isMinifyEnabled()) {

            //apply mapping混淆文件
            applyPatchMappingTxt(project,variant,mappingPath)
            def proguardTask = project.tasks.findByName("transformClassesAndResourcesWithProguardFor${variant.name.capitalize()}")

            File mappingFile = new File(mappingPath)

            if(proguardTask&&mappingFile.exists()){
                proguardTask.doLast({
                    project.getLogger().error("begin proguardTask")

                    def proguardHelper = new ProguardHelper(project,mappingPath,
                            Constants.getHotfixPath(project,Constants.HOTFIX_CLASSES),extension)
                    proguardHelper.copyProguardClass()

                })
            }else{
                project.getLogger().error("proguardTask not find")

            }
        }else{
        }
    }


    DefaultTask initGeneratePatchTask(Project project,
                                      ApplicationVariant variant,
                                      String classDir,
                                      String dexFile,
                                      File inStoreFile,
                                      String inStorePassword,
                                      String inKeyPassword,
                                      String inKeyAlias,
                                      boolean inSoPatch,
                                      boolean inOnlyARM){

        def generatePatchTask = project.task("generate${variant.name.capitalize()}Patch",type:GeneratePatchTask){
            inClassDir =classDir
            outDexPath = dexFile
            description '将混淆后的文件dex'
            storeFile = inStoreFile
            keyPassword = inKeyPassword
            keyAlias= inKeyAlias
            storePassword=inStorePassword
            soPatch = inSoPatch
            onlyARM = inOnlyARM
        }

        return generatePatchTask
    }


}
