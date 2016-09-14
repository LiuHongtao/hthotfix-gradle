/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

import com.netease.hearttouch.hthotfix.inject.HackInjector;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

/**
 * Created by zw on 16/6/21.
 */
public class ResourceDiffChecker {

    /***
     * 检测到资源文件后立即抛出错误
     * @param className
     */
    public static void  checkClass(Project project,String className){
        String internalClassName = className.replace('/','.');
        for(String resourceClassName: HackInjector.RESOURCE_CLASS_NAME){
            if(internalClassName.endsWith(resourceClassName)){
                String exceptionMessage = internalClassName + "has changed!";
                project.getLogger().error(exceptionMessage);
                throw new GradleException(exceptionMessage);
            }
        }
    }
}
