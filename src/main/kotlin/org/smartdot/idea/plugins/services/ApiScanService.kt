package org.smartdot.idea.plugins.services

import cn.hutool.json.JSONObject
import com.easy.doc.builder.ApiDataBuilder
import com.easy.doc.model.ApiConfig
import com.easy.doc.model.ApiDoc
import com.easy.doc.model.ApiMethodDoc
import com.easy.doc.model.SourceCodePath
import com.google.common.collect.Lists
import com.intellij.openapi.components.Service
import org.apache.commons.lang3.StringUtils
import org.smartdot.idea.plugins.Bundle
import org.smartdot.idea.plugins.bo.ApiBO
import org.smartdot.idea.plugins.consts.ProjectConsts
import java.io.File

@Service(Service.Level.PROJECT)
class ApiScanService {
    fun doScan(path: String): Collection<ApiBO> {
        val config = ApiConfig()
        config.sourceCodePaths = initPaths(path)
        config.codePath = joinSrcPath("")
        config.baseDir = path
        val api = ApiDataBuilder.getApiData(config)
        return initApis(api.apiDocList)
    }

    private fun initPaths(path: String): MutableList<SourceCodePath>? {
        val file = File(path)
        if (!file.exists() || file.isFile) {
            return Lists.newArrayList()
        }
        val paths: MutableList<SourceCodePath> = Lists.newArrayList()
        // 判断是否有src目录
        val src = containsSrc(path)
        if (src) {
            paths.add(SourceCodePath.builder().setPath(joinSrcPath(path)))
        }
        forPaths(file, paths)
        return paths
    }

    private fun forPaths(
        file: File,
        paths: MutableList<SourceCodePath>,
    ) {
        file.listFiles()?.forEach {
            if (containsSrc(it.absolutePath)) {
                paths.add(SourceCodePath.builder().setPath(joinSrcPath(it.absolutePath)))
            } else {
                if (it.isDirectory) {
                    forPaths(it, paths)
                }
            }
        }
    }

    private fun joinSrcPath(path: String): String = path + File.separator + "main" + File.separator + "java"

    private fun containsSrc(path: String): Boolean {
        val file = File(path)
        if (!file.exists() || file.isFile) {
            return false
        }
        val src = File(joinSrcPath(path))
        return src.exists() && src.isDirectory
    }

    private fun initApis(cls: List<ApiDoc>): ArrayList<ApiBO> {
        val list = ArrayList<ApiBO>()
        cls.forEach {
            val apis = it.list
            apis.forEach { m ->
                val url = m.url
                list.add(ApiBO(url, parseHttpParams(m), parseHttpMethod(m.type, m.contentType)))
            }
        }
        return list
    }

    private fun parseHttpParams(m: ApiMethodDoc): String {
        val params = m.requestExample ?: return ""
        if (params.isJson) {
            return params.jsonBody
        }
        val json = JSONObject()
        params.formDataList?.forEach {
            json.set(it.key, it.value)
        }
        return json.toJSONString(0)
    }

    private fun parseHttpMethod(
        m: String,
        contentType: String,
    ): String =
        if (Bundle.message("methodGet") == m) {
            Bundle.message("methodGet")
        } else if (ProjectConsts.POST_METHOD == m &&
            StringUtils.containsIgnoreCase(
                contentType,
                ProjectConsts.JSON_TYPE,
            )
        ) {
            Bundle.message("methodPostJson")
        } else {
            Bundle.message("methodPostForm")
        }

    fun wrapUrl(url: String): String {
        var input = url + ""
        val ch = "//"
        while (input.contains(ch)) {
            input = input.replace(ch, "/")
        }
        return input
    }
}
