/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix
import com.android.build.api.transform.*
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.netease.hearttouch.hthotfix.refclass.RefScanInstrument
import org.gradle.api.Project
import com.netease.hearttouch.hthotfix.patch.PatchGeneratorTransformExecutor
import com.netease.hearttouch.hthotfix.HTHotfixExtension
import com.netease.hearttouch.hthotfix.patch.PatchRefGeneratorTransformExecutor

/**
 *  将有变化的class文件写到指定目录
 */
public class PatchRefGeneratorTransform extends Transform {

    private final String NAME ="PatchRefGenerator"
    private final Project project
    private  HTHotfixExtension hotfixExtension
    private  PatchRefGeneratorTransformExecutor executor;
    private final RefScanInstrument refScanInstrumentContext;
    public PatchRefGeneratorTransform(Project project,HTHotfixExtension extension,RefScanInstrument refScanInstrumentContext) {
        this.project = project
        this.hotfixExtension = extension
        this.refScanInstrumentContext = refScanInstrumentContext;
    }

    @Override
    public void transform(
             Context context,
             Collection<TransformInput> inputs,
             Collection<TransformInput> referencedInputs,
             TransformOutputProvider outputProvider,
            boolean isIncremental) throws IOException, TransformException, InterruptedException {
        executor = new PatchRefGeneratorTransformExecutor(project,hotfixExtension,refScanInstrumentContext)

        inputs.each {
            def directoryInputs = it.getDirectoryInputs()
            //文件夹遍历
            directoryInputs.each { DirectoryInput directoryInput ->
                File dest = outputProvider.getContentLocation(
                        directoryInput.getFile().getName(),
                        directoryInput.contentTypes,
                        directoryInput.scopes,
                        Format.DIRECTORY);
                executor.transformDirectory(directoryInput.file, dest)
            }

            //jar遍历
            it.getJarInputs().each { JarInput jarInput->
                File inputFile = jarInput.getFile();
                //重命名输出文件,因为可能同名,会覆盖
                def destName = inputFile.getName();
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4);
                }
                destName = destName + "_" + inputFile.hashCode();
                //获得输出文件
                File dest = outputProvider.getContentLocation(
                        destName,
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR);
                executor.transformJar(jarInput.getFile(), dest)
            }
        }
        executor.transformFinish();
    }

    @Override
    public String getName() {
        return NAME
    }

    @Override
    Set<ContentType> getInputTypes() {
        return Collections.singleton(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<Scope> getScopes() {
        return Collections.singleton(QualifiedContent.Scope.PROJECT)
//        return EnumSet.of(QualifiedContent.Scope.PROJECT,
//                QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
//                QualifiedContent.Scope.SUB_PROJECTS,
//                QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
//                QualifiedContent.Scope.EXTERNAL_LIBRARIES,
//        )
    }

    @Override
    Set<Scope> getReferencedScopes() {
        return Collections.singleton(QualifiedContent.Scope.TESTED_CODE)
    }

    @Override
    public boolean isIncremental() {
        return false
    }
}
