/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.refclass;

import org.gradle.api.Project;
import org.objectweb.asm.*;
import com.netease.hearttouch.hthotfix.refclass.RefScanInstrument.*;

/**
 * Created by zw on 16/6/12.
 */
public class RefScanClassVisitor extends ClassVisitor{

    private Project project;
    private RefScanContext refScanContext;

    public RefScanClassVisitor(Project project,RefScanContext refScanContext){
        super(Opcodes.ASM4);
        this.project = project;
        this.refScanContext = refScanContext;
    }

    public void visit(int version, int access, String name, String sig, String superName, String[] interfaces) {
        super.visit(version,access,name,sig,superName,interfaces);

        if(!refScanContext.getReferenceWithPatch()){
            if(name!=null&&name.length()>0){
                //DONE
                refScanContext.checkPatchSuperClass(name.replace("/", "."));
            }

            if(!refScanContext.getReferenceWithPatch()&&interfaces!=null){
                for(String superInterface:interfaces){
                    if(refScanContext.checkPatchSuperClass(superInterface.replace("/","."))){
                        break;
                    }
                }
            }
        }
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value){

        if(!refScanContext.getReferenceWithPatch()) {
            String fieldClassName =  Type.getType(desc).getClassName();
            //删除数组后缀
            fieldClassName = fieldClassName.replace("[]","");
            refScanContext.checkInPatchClasses(fieldClassName);
        }

        return super.visitField(access,name,desc,signature,value);
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, String desc,
                                     String signature, String[] exceptions) {
        if(!refScanContext.getReferenceWithPatch()) {

            String  returnTypeClass =   Type.getReturnType(desc).getClassName();
            if(returnTypeClass!=null&&returnTypeClass.length()>0){
                returnTypeClass =  returnTypeClass.replace("[]","");
                refScanContext.checkInPatchClasses(returnTypeClass);
            }

            if(!refScanContext.getReferenceWithPatch()){
                Type[] paramsTypes =   Type.getArgumentTypes(desc);
                for(Type paramsType:paramsTypes){
                    String paramClassName = paramsType.getClassName();
                    if(paramClassName!=null&&paramClassName.length()>0){
                        paramClassName = paramClassName.replace("[]", "");
                        if(refScanContext.checkInPatchClasses(paramClassName)){
                            break;
                        }
                    }
                }
            }
        }

        if(!refScanContext.getReferenceWithPatch()&&exceptions!=null){
            for(String exception:exceptions){
                if(exception!=null&&exception.length()>0){
                    exception  =exception.replace("/",".");
                    if(refScanContext.checkInPatchClasses(exception)){
                        break;
                    }
                }
            }
        }

        if(refScanContext.getReferenceWithPatch())
            return super.visitMethod(access,name,desc,signature,exceptions);
        else{
            MethodVisitor mv = super.visitMethod(access,name,desc,signature,exceptions);
            return new RefMethodVisitor(project,mv,refScanContext);
        }
    }
}
