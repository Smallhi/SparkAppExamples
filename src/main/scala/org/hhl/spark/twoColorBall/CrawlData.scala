package org.hhl.spark.twoColorBall

import java.io.FileWriter
import java.util.concurrent.atomic.AtomicInteger

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.{Failure, Random, Success, Try}
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool

/**
  * Created by huanghl4 on 2017/11/17.
  */
object CrawlData {
  val ips = ProxyIP.crawlProxyIP(10, 1)
  val failedPageUrlList = new ListBuffer[String]

  def main(args: Array[String]): Unit = {

    concurrentCrawl2ColorBallData(73, 30)
    //for(i<- 1 to 73) crawl2ColorBallData(i.toString)
    crawlWx
    // 将失败的数据落地]
    val failedUrl = new FileWriter("/Users/hhl/mypro/SparkAppExamples/fail.txt", true)
    failedUrl.write(failedPageUrlList.toList.mkString + "\n")
    failedUrl.close()
  }

  def crawl2ColorBallData(page: String) = {
    val pageUrl = s"http://kj.cjcp.com.cn/ssq/index.php?topage=$page"
    println("当前解析路径：" + pageUrl)
    val doc = Jsoup.connect(pageUrl).get()
    val out = new FileWriter("/Users/hhl/mypro/SparkAppExamples/Hello.txt", true)
    val result = doc.select("tr.fen").listIterator().asScala.toList
    for (e <- result) {
      val qh = e.select("td.qihao").html()
      val time = e.select("td.time").html()
      val blueNum = e.select("input.q_blue").attr("value")
      val redNum = e.select("input.q_red").listIterator().asScala.toList.map(x => x.attr("value")).mkString(";")
      val money = e.select("td.t_center").listIterator().asScala.toList.map(x => x.ownText())
      val line = qh + ";" + time + ";" + redNum + ";" + blueNum + ";" + money(1) + ";" + money(2)
      out.write(line + "\n")
    }
    out.close()
  }

  def concurrentCrawl2ColorBallData(maxPage: Int, threadNum: Int) = {
    val loopPar = (1 to maxPage).par
    loopPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    loopPar.foreach(x => crawl2ColorBallData(x.toString))
  }


  def crawlWeatherData(year: String, month: String, day: String) = {
    val pageUrl = s"https://www.wunderground.com/history/airport/ZBAA/$year/$month/$day/DailyHistory.html?req_city=Beijing&req_state=&req_statename=China&reqdb.zip=&reqdb.magic=&reqdb.wmo="
    println("当前解析路径：" + pageUrl)
    val doc = promiseGetUrl(100, 30000, pageUrl)
    val out1 = new FileWriter("/Users/hhl/mypro/SparkAppExamples/wx.txt", true)
    val wx = doc.getElementById("observations_details").select("tr.no-metars").listIterator().asScala.toList
      .map(x => x.select("td").eachText().asScala.toList.mkString(";")).filter(_.contains("9:00 PM;"))
    //wx.foreach(println)
    //时间 (CST)	气温	风冷温	露点	湿度	气压	能见度	Wind Dir	风速	瞬间风速	Precip	活动	状况
    out1.write(year + month + day + ";" + wx.mkString + "\n")
    out1.close()
  }

  def crawlWx = {
    val date = Source.fromFile("/Users/hhl/mypro/SparkAppExamples/Hello.txt").getLines().filter(!_.contains("16115期"))
      .map(_.split(";")).map(x => x(1)).map(x => x.split("-")).toList.par
    date.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(50)) // 设置并发线程数
    date.foreach(x => {
      // 利用并发集合多线程同步抓取
      val y = x(0)
      val m = x(1)
      val d = x(2)
      crawlWeatherData(y, m, d)
    })
  }

  def sleep(i: Long) = Thread.sleep(i)

  val aiAll, aiCnt, aiFail: AtomicInteger = new AtomicInteger(0)

  // 利用递归实现自动重试（重试100次，每次休眠30秒）
  def promiseGetUrl(times: Int = 100, delay: Long = 30000, url: String): Document = {
    val ip = getProxyIp
    Try(Jsoup.connect(url).proxy(ip._1,ip._2).get()) match {
      case Failure(e) =>
        if (times != 0) {
          println(e.getMessage)
          aiFail.addAndGet(1)
          println("sleeping :" + url)
          sleep(delay)
          promiseGetUrl(times - 1, delay, url)
        } else {
          // println("failed" + url)
          // 将未爬出的链接扔到另一个文件中，重新爬取
          println("当前失败路径" + url)
          failedPageUrlList.append(url)
          throw e
        }
      case Success(d) => Jsoup.connect(url).proxy(ip._1,ip._2).get()
    }
    //aiAll.addAndGet(all); aiCnt.addAndGet(obj);
  }

  def getProxyIp: (String, Int) = {
    ips((new Random).nextInt(ips.size))
  }


}
