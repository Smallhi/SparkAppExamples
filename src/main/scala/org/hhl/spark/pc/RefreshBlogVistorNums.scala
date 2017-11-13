package org.hhl.spark.pc
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.sys.process._
/**
  * Created by huanghl4 on 2017/11/12.
  */
object RefreshBlogVistorNums {

  def main(args: Array[String]): Unit = {
    val l = blogList
    for (i <- 1 to 100) {
      l.foreach(x=>{
        val url = x
        println(s"当前round${i}的URL是:" + url)
        val pe = s"python3 /Users/hhl/test.py $url"!

      })
    }
  }

  def blogList:List[String] = {
    val mainPageUrl = "http://www.jianshu.com/u/ea9356f37a06"
    val doc = Jsoup.connect(mainPageUrl).get()
    val hrefs = doc.select("a.title").eachAttr("href").asScala.toList
    // fixme scala 循环如何像 java element: Elements 的写法？
   // val title = doc.select("a.title").eachText().asScala.toList
    hrefs.map(x=>"http://www.jianshu.com/" +x)
  }

}
