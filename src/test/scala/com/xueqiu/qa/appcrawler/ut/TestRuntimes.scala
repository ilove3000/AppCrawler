package com.xueqiu.qa.appcrawler.ut

import java.io.File
import java.io.File
import java.util.jar.JarFile

import com.twitter.util.Eval
import com.twitter.util.Eval.EvalSettings
import com.xueqiu.qa.appcrawler.plugin.DemoPlugin
import com.xueqiu.qa.appcrawler._
import org.scalatest.FunSuite
import org.xml.sax.ErrorHandler

import scala.reflect.internal.settings.MutableSettings
import scala.reflect.internal.util.ScalaClassLoader
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader
import scala.reflect.io.AbstractFile
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.{Global, Settings, GenericRunnerSettings}
import scala.tools.nsc.interpreter.IMain

/**
  * Created by seveniruby on 16/8/10.
  */
class TestRuntimes extends FunSuite with CommonLog{

  val fileName="/Users/seveniruby/projects/LBSRefresh/iOS_20160813203343/AppCrawler_8.scala"
  test("eval"){
    val result:Int=new Eval().inPlace("1+2")
    assert(result == 3)
  }
  test("eval println"){
    val result=new Eval().inPlace("println(\"xxx\")")
  }

  test("eval object invoke"){
    new Eval().inPlace("com.xueqiu.qa.appcrawler.MiniAppium.hello(\"dddd\", 333)")
  }

  test("MiniAppium dsl"){
    MiniAppium.dsl("hello(\"seveniruby\", 30000)")
    MiniAppium.dsl("hello(\"ruby\", 30000)")
    MiniAppium.dsl(" hello(\"seveniruby\", 30000)")
    MiniAppium.dsl("hello(\"seveniruby\", 30000 )  ")
    MiniAppium.dsl("sleep(3)")
    MiniAppium.dsl("hello(\"xxxxx\")")
  }

  test("compile"){
    val file=new File(fileName)
    val e=new Eval(Some(file))
    log.info(e.compilerOutputDir)

  }

  test("compile by scala"){
    Runtimes.init(new File(fileName).getParent)
    Runtimes.compile(List(fileName))



  }

  test("native compile"){


    val outputDir=new File(fileName).getParent

    val settings = new Settings()
    settings.deprecation.value = true // enable detailed deprecation warnings
    settings.unchecked.value = true // enable detailed unchecked warnings
    settings.outputDirs.setSingleOutput(outputDir)
    settings.usejavacp.value = true

    val global = new Global(settings)
    val run = new global.Run
    run.compile(List(fileName))

  }


  test("imain"){

    Runtimes.init()
    Runtimes.eval(
      """
        |import com.xueqiu.qa.appcrawler.MiniAppium
        |println("xxx")
        |println("ddd")
        |MiniAppium.hello("222")
      """.stripMargin)


  }


  test("imain q"){

    Runtimes.init()
    Runtimes.eval("import com.xueqiu.qa.appcrawler.MiniAppium")
    Runtimes.eval(
      """
        |println("xxx")
        |println("ddd")
        |MiniAppium.hello("222")
      """.stripMargin)


  }


  test("imain with MiniAppium"){

    Runtimes.init()
    Runtimes.eval("import com.xueqiu.qa.appcrawler.MiniAppium._")
    Runtimes.eval(
      """
        |hello("222")
        |println(driver)
      """.stripMargin)
  }

  test("compile plugin"){
    Runtimes.init()
    Runtimes.compile(List("src/universal/plugins/DynamicPlugin.scala"))
    val p=Class.forName("com.xueqiu.qa.appcrawler.plugin.DynamicPlugin").newInstance()
    log.info(p)


  }

  test("test classloader"){
    val classPath="target/tmp/"
    Runtimes.init(classPath)
    Runtimes.compile(List("/Users/seveniruby/projects/LBSRefresh/src/universal/plugins/"))
    val urls=Seq(new java.io.File(classPath).toURI.toURL)
    val loader=new URLClassLoader(urls, ClassLoader.getSystemClassLoader)
    val x=loader.loadClass("AppCrawler_5").newInstance().asInstanceOf[FunSuite]
    log.info(x.testNames)
    log.info(getClass.getCanonicalName)

    log.info(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)

  }

  test("load plugins"){

    val a=new DemoPlugin()
    log.info(a.asInstanceOf[Plugin])
    //getClass.getClassLoader.asInstanceOf[URLClassLoader].loadClass("DynamicPlugin")
    val plugins=Runtimes.loadPlugins("/Users/seveniruby/projects/LBSRefresh/src/universal/plugins/")
    plugins.foreach(log.info)

  }


}

