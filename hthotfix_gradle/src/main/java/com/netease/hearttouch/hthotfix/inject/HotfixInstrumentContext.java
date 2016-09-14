/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix.inject;

/**
 * Created by zw on 16/6/17.
 */
public class HotfixInstrumentContext {

    private String className;

    public String getClassName() {
        return className;
    }

     void setClassName(String className) {
        this.className = className;
    }

    public String getSupperClassName() {
        return supperClassName;
    }

     void setSupperClassName(String supperClassName) {
        this.supperClassName = supperClassName;
    }

    private String supperClassName;

    private boolean classModified;
    private boolean isIgnoreClass;

    public void reset(){
        this.className = null;
        this.supperClassName = null;
    }

    public boolean isClassModified(){
        return classModified;
    }

    public void setClassModified(boolean classModified){
        this.classModified = classModified;
    }

    public boolean isIgnoreClass(){
        return this.isIgnoreClass;
    }

    public  void setIgnoreClass(boolean isIgnoreClass){
        this.isIgnoreClass = isIgnoreClass;
    }


}
