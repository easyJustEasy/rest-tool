package org.smartdot.idea.plugins.services

import cn.hutool.http.ContentType
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONObject
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.text.StringUtil
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.smartdot.idea.plugins.Bundle
import org.smartdot.idea.plugins.bo.RequestBO
import java.net.HttpCookie
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class RequestService {
    fun request(requestBO: RequestBO): String {
        if (StringUtil.isEmpty(requestBO.method)) {
            return ""
        }
        if (StringUtil.isEmpty(requestBO.url)) {
            return ""
        }
        var json = JSONObject()
        val headJson: JSONObject
        val hederMap: MutableMap<String, String> = HashMap()
        if (StringUtil.isNotEmpty(requestBO.params)) {
            json = JSONObject(requestBO.params)
        }

        if (StringUtil.isNotEmpty(requestBO.header)) {
            headJson = JSONObject(requestBO.header)
            for (key in headJson.keys) {
                hederMap[key] = headJson.getStr(key)
            }
        }
        val cookieJson: JSONObject
        val cookies = ArrayList<HttpCookie>()
        if (StringUtil.isNotEmpty(requestBO.cookie)) {
            cookieJson = JSONObject(requestBO.cookie)
            for (key in cookieJson.keys) {
                val c = HttpCookie(key, cookieJson.getStr(key))
                cookies.add(c)
            }
        }

        when (requestBO.method) {
            Bundle.message("methodGet") -> {
                val parameters: MutableList<NameValuePair> = ArrayList()
                for (key in json.keys) {
                    parameters.add(BasicNameValuePair(key, json.getStr(key)))
                }
                val queryString = EntityUtils.toString(UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8))
                return HttpUtil
                    .createGet(requestBO.url + "?" + queryString)
                    .addHeaders(hederMap)
                    .cookie(cookies)
                    .execute()
                    .body()
            }

            Bundle.message("methodPostForm") -> {
                val paramsMap: MutableMap<String, Any> = HashMap()
                for (key in json.keys) {
                    paramsMap[key] = json[key].toString()
                }
                return HttpUtil
                    .createPost(requestBO.url)
                    .header("Content-Type", ContentType.FORM_URLENCODED.toString())
                    .addHeaders(hederMap)
                    .cookie(cookies)
                    .form(paramsMap)
                    .execute()
                    .body()
            }

            else ->
                return HttpUtil
                    .createPost(requestBO.url)
                    .header("Content-Type", ContentType.JSON.toString())
                    .addHeaders(hederMap)
                    .cookie(cookies)
                    .body(json.toJSONString(0))
                    .execute()
                    .body()
        }
    }
}
