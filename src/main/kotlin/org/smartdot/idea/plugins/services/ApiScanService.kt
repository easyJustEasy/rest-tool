package org.smartdot.idea.plugins.services

import cn.hutool.core.collection.CollectionUtil
import cn.hutool.core.math.BitStatusUtil.has
import cn.hutool.json.JSONObject
import com.intellij.openapi.components.Service
import com.thoughtworks.qdox.JavaProjectBuilder
import com.thoughtworks.qdox.model.JavaAnnotation
import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.JavaMethod
import com.thoughtworks.qdox.model.JavaParameter
import com.thoughtworks.qdox.model.expression.AnnotationValue
import org.smartdot.idea.plugins.Bundle
import org.smartdot.idea.plugins.bo.ApiBO
import java.io.File

@Service(Service.Level.PROJECT)
class ApiScanService() {
    fun doScan(path: String): Collection<ApiBO> {
        val builder = JavaProjectBuilder()
        builder.addSourceTree(File(path))
        val cls: Collection<JavaClass> = builder.classes
        val map = initMap(cls)
        val ctrls = findAllCtrls(cls)
        val apis = initApis(ctrls, map)
        return apis;
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
            if (hasSomeAnnotation(it, "RestController") != null || hasSomeAnnotation(it, "Controller") != null) {
                list.add(it)
            }
        }
        return list
    }

    private fun initApis(cls: Collection<JavaClass>, map: HashMap<String, JavaClass>): Collection<ApiBO> {
        val list = ArrayList<ApiBO>()
        cls.forEach {
            val an = hasSomeAnnotation(it, "RequestMapping")
            val isRest = hasSomeAnnotation(it, "RestController")
            if (an != null) {
                var url = annotationValueToString(an.getProperty("value"))
                val methods = it.methods
                if (CollectionUtil.isNotEmpty(methods)) {
                    methods.forEach {
                        if (isPostRequestMapping(it) || isGetRequestMapping(it) || isRequestMapping(it)) {
                            list.add(
                                ApiBO(
                                    wrapUrl(parseHttpUrl(url, it)),
                                    parseHttpParams(it, map),
                                    parseHttpMethod(it, isRest, map)
                                )
                            )
                        }
                    }
                }
            }

        }
        return list
    }

    private fun parseHttpUrl(url: String, it: JavaMethod): String {
        val getMapping = hasSomeMapping(it, "GetMapping")
        val postMapping = hasSomeMapping(it, "PostMapping")
        val requestMapping = hasSomeMapping(it, "RequestMapping")
        var an: JavaAnnotation? = null
        if (getMapping != null) {
            an = getMapping
        } else if (postMapping != null) {
            an = postMapping
        } else {
            an = requestMapping
        }
        return buildString {
            append("/")
            append(url)
            append("/")
            append(annotationValueToString(an?.getProperty("value")))
        }
    }

    private fun parseHttpMethod(it: JavaMethod, body: JavaAnnotation?, map: HashMap<String, JavaClass>): String {
        if (isGetRequestMapping(it)) {
            return Bundle.message("methodGet")
        } else if (isPostRequestMapping(it) && hasPostBody(it, body)) {
            return Bundle.message("methodPostJson")
        } else {
            return Bundle.message("methodPostForm")
        }
    }

    private fun isGetRequestMapping(it: JavaMethod): Boolean {
        return hasSomeMapping(it, "GetMapping") != null
    }

    private fun hasSomeMapping(it: JavaMethod, mapping: String): JavaAnnotation? {
        val annotation =
            it.annotations.filter {
                val fullName = it.type.fullyQualifiedName
                ("org.springframework.web.bind.annotation." + mapping).equals(fullName)
            }
        if (CollectionUtil.isNotEmpty(annotation)) {
            return annotation.get(0)
        }
        return null
    }

    private fun hasSomeAnnotation(it: JavaClass, mapping: String): JavaAnnotation? {
        val annotation =
            it.annotations.filter {
                val fullName = it.type.fullyQualifiedName
                ("org.springframework.web.bind.annotation." + mapping).equals(fullName)
            }
        if (CollectionUtil.isNotEmpty(annotation)) {
            return annotation.get(0)
        }
        return null
    }

    private fun isRequestMapping(it: JavaMethod): Boolean {
        return hasSomeMapping(it, "RequestMapping") != null
    }

    private fun isPostRequestMapping(it: JavaMethod): Boolean {
        return hasSomeMapping(it, "PostMapping") != null
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
            val pv = map.get(it.type.fullyQualifiedName)
            if (pv != null) {
                json.set(it.name, parseObjJson(pv, map))
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
            val pr = map.get(it.type.fullyQualifiedName)
            if (pr != null) {
                v.set(it.name, parseObjJson(pr, map))
            } else {
                v.set(it.name, it.type.value.toString())
            }
        }
        return v
    }

    fun wrapUrl(url: String): String {
        return url.replace("//", "/")
    }

    private fun annotationValueToString(annotationValue: AnnotationValue?): String {
        return if (annotationValue == null) {
            ""
        } else if (annotationValue.getParameterValue() is String) {
            val parameterValue = annotationValue.getParameterValue() as String
            parameterValue.substring(1, parameterValue.length - 1)
        } else {
            annotationValue.getParameterValue().toString()
        }
    }
}