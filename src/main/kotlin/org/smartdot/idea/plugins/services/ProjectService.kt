package org.smartdot.idea.plugins.services

import cn.hutool.http.ContentType
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.google.common.collect.Lists
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import org.smartdot.idea.plugins.Bundle
import org.smartdot.idea.plugins.bo.RequestBO
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import java.net.HttpCookie
import java.nio.charset.StandardCharsets
import javax.swing.JOptionPane

@Service(Service.Level.PROJECT)
class ProjectService() {


    fun request(requestBO: RequestBO): String {

        if (StringUtil.isEmpty(requestBO.method)) {
            JOptionPane.showMessageDialog(null, "method can not be blank")
            return "";
        }
        if (StringUtil.isEmpty(requestBO.url)) {
            JOptionPane.showMessageDialog(null, "url can not be blank")
            return "";
        }
        var json = JSONObject()
        var headJson = JSONObject()
        val hederMap: MutableMap<String, String> = HashMap()
        if (StringUtil.isNotEmpty(requestBO.params)) {
            json = JSONObject(requestBO.params)
        }

        if (StringUtil.isNotEmpty(requestBO.header)) {
            headJson = JSONObject(requestBO.header)
            for (key in headJson.keys) {
                hederMap.put(key, headJson.getStr(key))
            }
        }
        var cookieJson = JSONObject()
        val cookies = ArrayList<HttpCookie>()
        if (StringUtil.isNotEmpty(requestBO.cookie)) {
            cookieJson = JSONObject(requestBO.cookie)
            for (key in cookieJson.keys) {
                val c = HttpCookie(key,cookieJson.getStr(key))
                cookies.add(c)
            }
        }

        if (Bundle.message("methodGet").equals(requestBO.method)) {
            val parameters: MutableList<NameValuePair> = ArrayList()
            for (key in json.keys) {
                parameters.add(BasicNameValuePair(key, json.getStr(key)))
            }
            val queryString = EntityUtils.toString(UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8))
            return HttpUtil.createGet(requestBO.url + "?" + queryString)
                .addHeaders(hederMap)
                .cookie(cookies)
                .execute()
                .body()
        } else if (Bundle.message("methodPostForm").equals(requestBO.method)) {
            val paramsMap: MutableMap<String, Any> = HashMap()
            for (key in json.keys) {
                paramsMap.put(key, json.get(key).toString())
            }
            return HttpUtil.createPost(requestBO.url)
                .header("Content-Type", ContentType.FORM_URLENCODED.toString())
                .addHeaders(hederMap)
                .cookie(cookies)
                .form(paramsMap).execute().body()
        } else {
            return HttpUtil.createPost(requestBO.url)
                .header("Content-Type", ContentType.JSON.toString())
                .addHeaders(hederMap)
                .cookie(cookies)
                .body(json.toJSONString(0))
                .execute().body()
        }
    }
}
