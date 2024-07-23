package org.smartdot.idea.plugins.bo

import cn.hutool.json.JSONObject

class ApiBO(val url:String, var param: String, val method:String) {
    override fun toString(): String {
        return "ApiBO(url='$url', param=$param, method='$method')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApiBO

        if (url != other.url) return false
        if (param != other.param) return false
        if (method != other.method) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + param.hashCode()
        result = 31 * result + method.hashCode()
        return result
    }

    companion object {
        fun newBO(): ApiBO {
        return ApiBO("","","")
        }
    }
}