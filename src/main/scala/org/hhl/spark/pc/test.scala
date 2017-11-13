package org.hhl.spark.pc

import org.jsoup.Jsoup
/**
  * Created by huanghl4 on 2017/11/12.
  */
object test {

  def main(args: Array[String]): Unit = {
    val url = "http://www.jianshu.com/p/c20351c842bc"
    //val url = "http://blog.csdn.net/googdev/article/details/52575079"
    val ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36"
    val ref = "http://www.jianshu.com/p/c20351c842bc"
    for (i <- 1 to 1) {
      Thread.sleep(2000)
      val doc = Jsoup.connect(url)
        .userAgent(ua)
        .referrer(ref)
        .get()
      val body = doc.body().select("div.meta").select("span.wordage").text()//.select("txt")
      println(body.toString)
      println(i+" refresh" + s" vistors are $body")
    }
  }
}
