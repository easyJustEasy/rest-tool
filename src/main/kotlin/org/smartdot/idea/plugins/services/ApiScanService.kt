package org.smartdot.idea.plugins.services

import cn.hutool.json.JSONObject
import com.google.common.collect.Lists
import com.intellij.openapi.components.Service
import com.power.doc.builder.ApiDataBuilder
import com.power.doc.model.ApiConfig
import com.power.doc.model.ApiDoc
import com.power.doc.model.ApiMethodDoc
import com.power.doc.model.SourceCodePath
import org.apache.commons.lang3.StringUtils
import org.smartdot.idea.plugins.Bundle
import org.smartdot.idea.plugins.bo.ApiBO
import org.smartdot.idea.plugins.consts.ProjectConsts

@Service(Service.Level.PROJECT)
class ApiScanService {


    fun doScan(path: String): Collection<ApiBO> {
        val config = ApiConfig()
        val paths = Lists.newArrayList(SourceCodePath.builder().setPath(path))
        config.sourceCodePaths = paths
        val api = ApiDataBuilder.getApiData(config)
        return initApis(api.apiDocList)
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

    private fun parseHttpMethod(m: String, contentType: String): String {
        return if (Bundle.message("methodGet") == m) {
            Bundle.message("methodGet")
        } else if (ProjectConsts.POST_METHOD == m && StringUtils.containsIgnoreCase(
                contentType,
                ProjectConsts.JSON_TYPE
            )
        ) {
            Bundle.message("methodPostJson")
        } else {
            Bundle.message("methodPostForm")
        }
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