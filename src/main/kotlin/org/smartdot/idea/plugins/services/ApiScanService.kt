package org.smartdot.idea.plugins.services

import cn.hutool.core.collection.CollectionUtil
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.google.common.collect.Lists
import com.intellij.openapi.components.Service
import com.jetbrains.rd.util.put
import com.thoughtworks.qdox.JavaProjectBuilder
import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.expression.AnnotationValue
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
            val ans = it.annotations
            val mt = it
            ans.forEach {
                val name = it.type.fullyQualifiedName
                if (name.equals("org.springframework.web.bind.annotation.RestController") || name.equals("org.springframework.web.bind.annotation.Controller")) {
                    list.add(mt)
                }
            }
        }
        return list
    }

    private fun initApis(cls: Collection<JavaClass>, map: HashMap<String, JavaClass>): Collection<ApiBO> {
        val list = ArrayList<ApiBO>()
        cls.forEach {
            val an = it.annotations.filter {
                it.type.fullyQualifiedName.equals("org.springframework.web.bind.annotation.RequestMapping")
            }
            if (CollectionUtil.isNotEmpty(an)) {
                var url = annotationValueToString(an.get(0).getProperty("value"))
                val methods = it.methods
                if (CollectionUtil.isNotEmpty(methods)) {
                    methods.forEach {
                        val get =
                            it.annotations.filter { it.type.fullyQualifiedName.equals("org.springframework.web.bind.annotation.RequestMapping") }
                        if (CollectionUtil.isNotEmpty(get)) {
                            val tmpUrl = "/" + url + "/" + annotationValueToString(get.get(0).getProperty("value"))
                            val params = it.parameters
                            val json = JSONObject()
                            params.filter {
                                !(it.fullyQualifiedName.equals("javax.servlet.http.HttpServletRequest")||it.fullyQualifiedName.equals("javax.servlet.http.HttpServletResponse"))
                            }.forEach {
                                val pv = map.get(it.type.fullyQualifiedName)
                                var v = it.type.value.toString()
                                if (pv != null) {
                                  v= parseObjJson(pv,map)
                                }
                                json.set(it.name, v)
                            }
                            list.add(ApiBO(wrapUrl(tmpUrl), json))
                        }
                    }
                }
            }

        }
        return list
    }
private fun parseObjJson(pv:JavaClass,map: HashMap<String, JavaClass>):String{
    val v = JSONObject()
    val fs = pv.fields
    fs.forEach{
        val pr = map.get(it.type.fullyQualifiedName)
        if(pr!=null){
            v.set(it.name,parseObjJson(pr,map))
        }else{
            v.set(it.name,it.type.value.toString())
        }
    }
    return v.toJSONString(0)
}
    private fun wrapUrl(url: String): String {
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