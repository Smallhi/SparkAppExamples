package org.hhl.spark.twoColorBall

import java.io.FileWriter
import java.util.regex.Pattern

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.util.{Success, Try}

/**
  * Created by huanghl4 on 2017/11/20.
  */
object ProxyIP {
  var testUrl = "https://www.wunderground.com/history/airport/ZBAA/2006/07/25/DailyHistory.html?req_city=Beijing&req_state=&req_statename=China&reqdb.zip=&reqdb.magic=&reqdb.wmo="

  def main(args: Array[String]): Unit = {
    run(10, 500)

    val rawIP = ips.distinct.par
    println("收集到IP个数：" + rawIP.size)
    rawIP.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(50))
//    rawIP.filter(x=>checkProxy(x._1,x._2)).foreach(x=>out2File(x._1+":"+x._2))
    rawIP.foreach(x=>{
      if(checkProxy(x._1,x._2)) out2File(x._1+":"+x._2)
      else println("不可用" + x._1+":"+x._2)
    })
  }

  val ips = new ListBuffer[(String,Int)]

  // 代理IP的网站
  val proxyIPWeb = List(
    "http://www.kuaidaili.com/free/intr/",
    "http://www.kuaidaili.com/free/inha/",
    "http://www.xicidaili.com/nn/",
    "http://www.xicidaili.com/nt/"
  )

  // Fixme 并发的循环如何中断
  def run(wantedNum: Int, totalPages: Int)= {
    val pl = proxyIPWeb.par
    //pl.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(50))
    //val lb = new ListBuffer[(String, Int)]
    pl.foreach(x => {
      // 对当前网站，并发爬虫代理IP
      println("当前网站->" + x )
      concurrentCrawlProxyIP(totalPages,100,x)
    })
  }

  def concurrentCrawlProxyIP(maxPage:Int,threadNum:Int,baseUrl:String) = {
    val loopPar = (1 to maxPage).par
    loopPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    loopPar.foreach(x => crawlProxyIPs(x,baseUrl))
  }

  def crawlProxyIPs(page:Int,baseUrl:String) = {
    val ipReg = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3} \\d{1,6}"
    val ipPtn = Pattern.compile(ipReg)
    val doc =
    Try(Jsoup.connect(baseUrl + page + "/").get()) match {
      case Success(d) => Jsoup.connect(baseUrl + page + "/").get()
      case _ => new Document("www.baidu.com")
    }
    //println(baseUrl + page + "/")
    val m = ipPtn.matcher(doc.text())
    while (m.find()) {
      val str = m.group().split(" ")
      ips.append((str(0),str(1).toInt))
//      println(baseUrl +page+ "/ ->" + str.mkString(":"))
//      if (checkProxy(str(0), str(1).toInt)) {
//        out2File(str.mkString(":"))
//        println(str.mkString(":") + "可用")
//      }
    }
  }

  def checkProxy(ip: String, port: Int): Boolean = {
    //Try(Jsoup.connect("http://1212.ip138.com/ic.asp")
    println("检测IP->" + ip + ":" + port)
    Try(Jsoup.connect(testUrl)
      .timeout(2 * 1000).proxy(ip, port)
      .get()) match {
      case Success(d) => true
      case _ => false
    }
  }

  def out2File(str: String) = {
    val out = new FileWriter("/Users/hhl/mypro/SparkAppExamples/proxyIP.txt", true)
    out.write(str + "\n")
    out.close()
  }
}
