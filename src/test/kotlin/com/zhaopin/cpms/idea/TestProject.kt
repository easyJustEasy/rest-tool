package com.zhaopin.cpms.idea

import com.thoughtworks.qdox.JavaProjectBuilder
import com.thoughtworks.qdox.directorywalker.SuffixFilter
import org.apache.tools.ant.DirectoryScanner
import org.junit.Test
import java.io.File
import java.io.IOException

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
    @Test
    fun projectBuilder() {
        val path = "E:\\work\\cpms\\cpms-task-todo"
        val builder = JavaProjectBuilder()
        val scanner = com.thoughtworks.qdox.directorywalker.DirectoryScanner(File(path))
        scanner.addFilter(SuffixFilter(".java"))
        scanner.scan { currentFile: File? ->
            try {
                builder.addSource(currentFile)
            } catch (e: com.thoughtworks.qdox.parser.ParseException) {
                println(e.message)
            } catch (e: IOException) {
                println(e.message)
            }
        }
        val clss = builder.classes
        println("builder"+clss)
    }
}