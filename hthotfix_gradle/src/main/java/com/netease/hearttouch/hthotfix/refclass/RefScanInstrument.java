/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.refclass;

import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zw on 16/6/30.
 */
public class RefScanInstrument {
    private final Project project;
    private boolean refWithPatch;
    private Set<String> patchClasses = new HashSet<String>();
    private Set<String> patchSuperClasses = new HashSet<String>();
    //private

    public RefScanInstrument(Project project){
        this.project = project;
    }

    public boolean isPatchClass(String className){
        return patchClasses.contains(className);
    }

    public void addPatchClassName(String className,String superClassName){
        if(className!=null){
            className = className.replace("/",".");
            project.getLogger().error("classname is;\t"+className);
            patchClasses.add(className);
        }

        if(superClassName!=null){

            superClassName  = superClassName.replace("/",".");
            project.getLogger().error("classname is;\t"+superClassName);

            patchSuperClasses.add(superClassName);
        }
    }

    /**
     * 和补丁是否有引用关系
     * @param classBytes
     * @return
     */
    public  boolean hasReference(byte[] classBytes,Project project){
        ClassReader cr = new ClassReader(classBytes);
        RefScanContext context = new RefScanContext(patchClasses,patchSuperClasses);
        RefScanClassVisitor cv = new RefScanClassVisitor(project,context);
        cr.accept(cv,0);
        return context.getReferenceWithPatch();
    }

    public static final class RefScanContext{
        private final Set<String> patchClasses;
        private final Set<String> patchSuperClasses;
        private boolean bReferenceWithPatch = false;
        private String className;

        public RefScanContext(Set<String> patchClasses,Set<String> patchSuperClasses){
            this.patchClasses = patchClasses;
            this.patchSuperClasses = patchSuperClasses;
        }


        public void setClassName(String className){
            this.className = className;
        }

        public boolean getReferenceWithPatch(){
            return this.bReferenceWithPatch;
        }

        public boolean checkInPatchClasses(String className){
            boolean bReference =  this.patchClasses.contains(className);
            if(bReference)
                this.bReferenceWithPatch = bReference;
            return bReference;
        }

        public boolean checkPatchSuperClass(String className){
            boolean bReference =  this.patchSuperClasses.contains(className);
            if(bReference)
                this.bReferenceWithPatch = bReference;
            return bReference;
        }
    }
}
