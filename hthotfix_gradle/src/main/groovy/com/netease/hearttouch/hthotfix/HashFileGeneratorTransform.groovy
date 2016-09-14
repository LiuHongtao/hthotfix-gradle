/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix

import com.android.build.api.transform.*
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.Scope
import org.gradle.api.Project
import com.netease.hearttouch.hthotfix.HTHotfixExtension

public class HashFileGeneratorTransform extends Transform {

    private final String HOTFIX_NAME ="HashFileGenerator"
    private final Project project
    private final HTHotfixExtension hotfixExtension
    private  HashTransformExecutor executor

    public HashFileGeneratorTransform(Project project,HTHotfixExtension extension) {
        this.project = project
        this.hotfixExtension = extension
    }

    @Override
    public void transform(
             Context context,
             Collection<TransformInput> inputs,
             Collection<TransformInput> referencedInputs,
             TransformOutputProvider outputProvider,
            boolean isIncremental) throws IOException, TransformException, InterruptedException {

        executor = new HashTransformExecutor(project,hotfixExtension)

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
                executor.transformJar(jarInput.getFile(),dest)
            }
        }

        executor.transformFinish();
    }

    @Override
    public String getName() {
        return HOTFIX_NAME
    }

    @Override
    Set<ContentType> getInputTypes() {
        return Collections.singleton(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<Scope> getScopes() {
        return Collections.singleton(QualifiedContent.Scope.PROJECT)
        return EnumSet.of(QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES,
        )
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
