package com.zhaopin.cpms.idea

import org.junit.Test

class TestProject {
    @Test
    fun main() {
        var input = "////"
        val ch = "//"
        while (input.contains(ch)){
          input =   input.replace(ch, "/")
        }
        println(input)
    }
}