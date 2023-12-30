package org.smartdot.idea.plugins.services

import cn.hutool.core.collection.CollectionUtil
import cn.hutool.json.JSONObject
import com.intellij.openapi.components.Service
import com.thoughtworks.qdox.JavaProjectBuilder
import com.thoughtworks.qdox.model.JavaAnnotation
import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.JavaMethod
import com.thoughtworks.qdox.model.expression.AnnotationValue
import org.smartdot.idea.plugins.Bundle
import org.smartdot.idea.plugins.bo.ApiBO
import org.smartdot.idea.plugins.consts.ProjectConsts
import java.io.File

@Service(Service.Level.PROJECT)
class ApiScanService {


    fun doScan(path: String): Collection<ApiBO> {
        val builder = JavaProjectBuilder()
        builder.addSourceTree(File(path))
        val cls: Collection<JavaClass> = builder.classes
        val map = initMap(cls)
        val ctrls = findAllCtrls(cls)
        return initApis(ctrls, map)
    }

    private fun initMap(cls: Collection<JavaClass>): HashMap<String, JavaClass> {
        val map = HashMap<String, JavaClass>()
        cls.forEach {
            map[it.fullyQualifiedName] = it
        }
        return map
    }


    private fun findAllCtrls(cls: Collection<JavaClass>): Collection<JavaClass> {
        val list = ArrayList<JavaClass>()
        cls.forEach {
            if (hasSomeAnnotation(it, ProjectConsts.REST_CONTROLLER) != null || hasSomeAnnotation(
                    it,
                    ProjectConsts.CONTROLLER
                ) != null
            ) {
                list.add(it)
            }
        }
        return list
    }

    private fun initApis(cls: Collection<JavaClass>, map: HashMap<String, JavaClass>): Collection<ApiBO> {
        val list = ArrayList<ApiBO>()
        cls.forEach { it ->
            val an = hasSomeAnnotation(it, ProjectConsts.REQUEST_MAPPING)
            var url = "/"
            if (an != null) {
                url = annotationValueToString(an.getProperty("value"))
            }
            val isRest = hasSomeAnnotation(it, ProjectConsts.REST_CONTROLLER)
            val methods = it.methods
            if (CollectionUtil.isNotEmpty(methods)) {
                methods.forEach {
                    if (isPostRequestMapping(it) || isGetRequestMapping(it) || isRequestMapping(it)) {
                        list.add(
                            ApiBO(
                                wrapUrl(parseHttpUrl(url, it)),
                                parseHttpParams(it, map),
                                parseHttpMethod(it, isRest)
                            )
                        )
                    }
                }
            }

        }
        return list
    }

    private fun parseHttpUrl(url: String, it: JavaMethod): String {
        val requestMapping = hasSomeMapping(it, ProjectConsts.REQUEST_MAPPING)
        val an: JavaAnnotation? =
            hasSomeMapping(it, ProjectConsts.GET_MAPPING) ?: (hasSomeMapping(it, ProjectConsts.POST_MAPPING)
                ?: requestMapping)
        return buildString {
            append("/")
            append(url)
            append("/")
            append(annotationValueToString(an?.getProperty("value")))
        }
    }

    private fun parseHttpMethod(it: JavaMethod, body: JavaAnnotation?): String {
        return if (isGetRequestMapping(it)) {
            Bundle.message("methodGet")
        } else if (isPostRequestMapping(it) && hasPostBody(it, body)) {
            Bundle.message("methodPostJson")
        } else {
            Bundle.message("methodPostForm")
        }
    }

    private fun isGetRequestMapping(it: JavaMethod): Boolean {
        return hasSomeMapping(it, ProjectConsts.GET_MAPPING) != null
    }

    private fun hasSomeMapping(it: JavaMethod, mapping: String): JavaAnnotation? {
        val imports = it.declaringClass.source.imports
        val hasSpring = imports.contains(ProjectConsts.SPRING_ANNOTATION_PKG + "*")
        val annotation =
            it.annotations.filter {
                val fullName = it.type.fullyQualifiedName
                (ProjectConsts.SPRING_ANNOTATION_PKG + mapping) == fullName || (mapping == fullName && hasSpring)
            }
        if (CollectionUtil.isNotEmpty(annotation)) {
            return annotation[0]
        }
        return null
    }

    private fun hasSomeAnnotation(it: JavaClass, mapping: String): JavaAnnotation? {
        val imports = it.source.imports
        val hasSpring = imports.contains(ProjectConsts.SPRING_ANNOTATION_PKG + "*")
        val annotation =
            it.annotations.filter {
                val fullName = it.type.fullyQualifiedName
                (ProjectConsts.SPRING_ANNOTATION_PKG + mapping) == fullName
                        || (mapping == fullName && hasSpring)


            }
        if (CollectionUtil.isNotEmpty(annotation)) {
            return annotation[0]
        }
        return null
    }

    private fun isRequestMapping(it: JavaMethod): Boolean {
        return hasSomeMapping(it, ProjectConsts.REQUEST_MAPPING) != null
    }

    private fun isPostRequestMapping(it: JavaMethod): Boolean {
        return hasSomeMapping(it, ProjectConsts.POST_MAPPING) != null
    }


    private fun hasPostBody(it: JavaMethod, rest: JavaAnnotation?): Boolean {
        return rest != null || hasSomeMapping(it, "RequestBody") != null
    }

    private fun parseHttpParams(it: JavaMethod, map: HashMap<String, JavaClass>): JSONObject {
        val params = it.parameters
        val json = JSONObject()
        params.filter {
            !(it.fullyQualifiedName.equals("javax.servlet.http.HttpServletRequest") || it.fullyQualifiedName.equals(
                "javax.servlet.http.HttpServletResponse"
            ))
        }.forEach {
            val pv = map[it.type.fullyQualifiedName]
            if (pv != null) {
                val j = parseObjJson(pv, map);
                j.keys.forEach{k ->
                    run { json.set(k, j[k]) }
                }
            } else {
                json.set(it.name, it.type.value.toString())
            }
        }
        return json
    }

    private fun parseObjJson(pv: JavaClass, map: HashMap<String, JavaClass>): JSONObject {
        val v = JSONObject()
        val fs = pv.fields
        fs.forEach {
            val pr = map[it.type.fullyQualifiedName]
            if (pr != null) {
                v.set(it.name, parseObjJson(pr, map))
            } else {
                v.set(it.name, it.type.value.toString())
            }
        }
        return v
    }

    fun wrapUrl(url: String): String {
        var input = url + ""
        val ch = "//"
        while (input.contains(ch)) {
            input = input.replace(ch, "/")
        }
        return input
    }

    private fun annotationValueToString(annotationValue: AnnotationValue?): String {
        return if (annotationValue == null) {
            ""
        } else if (annotationValue.parameterValue is String) {
            val parameterValue = annotationValue.parameterValue as String
            parameterValue.substring(1, parameterValue.length - 1)
        } else {
            annotationValue.parameterValue.toString()
        }
    }
}