/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.refclass;

import org.gradle.api.Project;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import java.util.Set;

/**
 * Created by zw on 16/6/12.
 */
public class RefMethodVisitor extends MethodVisitor {
    private Project project;
    private RefScanInstrument.RefScanContext context;

    public RefMethodVisitor(Project project, MethodVisitor mv, RefScanInstrument.RefScanContext refScanContext) {
        super(Opcodes.ASM4,mv);
        this.project = project;
        this.context = refScanContext;
    }

    @Override
    //visit LocalVariableTable
    public void visitLocalVariable( String name, String desc, String signature, Label start, Label end, int index) {
        if(desc!=null){
            String descName = Type.getType(desc).getClassName().replace("[]","");
            context.checkInPatchClasses(descName);
        }
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, type);

        //new-bb-187
        //create new object of type identified by class reference in constant pool index
        //可访问到匿名类,但无法获知匿名来源
        if (opcode == Opcodes.NEW) {
            context.checkInPatchClasses(type);
        }
    }

    @Override
    public void visitLdcInsn(Object cst) {
        super.visitLdcInsn(cst);

        //ldc-12-18
        //push a constant #index from a constant pool (String, int or float) onto the stack
        if (cst instanceof Type) {
            Type type = (Type) cst;
            int sort = type.getSort();
            //class literal : String.class
            //对void.class无效,void.class被当做getstatic
            if (sort == Type.OBJECT) {
                context.checkInPatchClasses(type.getClassName());
            }
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        super.visitFieldInsn(opcode, owner, name, desc);

        //getstatic-b2-178
        //get a static field value of a class, where the field is identified by field reference in the constant pool index
        //仅检查static字段,对static final字段无效(ldc)
        if (opcode == Opcodes.GETSTATIC) {
            context.checkInPatchClasses(owner);
        }
    }
}
