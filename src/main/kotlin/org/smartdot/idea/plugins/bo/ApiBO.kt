package org.smartdot.idea.plugins.bo

import cn.hutool.json.JSONObject

class ApiBO(val url:String, val param: JSONObject, val method:String) {
    override fun toString(): String {
        return "ApiBO(url='$url', param=$param, method='$method')"
    }
}