package org.hhl.spark.twoColorBall

import java.util.regex.Pattern

import org.jsoup.Jsoup

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.util.{Success, Try}
import scala.util.control.Breaks._

/**
  * Created by huanghl4 on 2017/11/20.
  */
object ProxyIP {

  var testUrl = "https://www.wunderground.com/history/airport/ZBAA/2006/07/25/DailyHistory.html?req_city=Beijing&req_state=&req_statename=China&reqdb.zip=&reqdb.magic=&reqdb.wmo="

  def main(args: Array[String]): Unit = {
    crawlProxyIP(10, 1)
  }
// 代理IP的网站
  val proxyIPWeb = List(
    "http://www.kuaidaili.com/free/intr/",
    "http://www.kuaidaili.com/free/inha/",
    "http://www.xicidaili.com/nn/",
    "http://www.xicidaili.com/nt/"
  )

  // Fixme 并发的循环如何中断
  def crawlProxyIP(wantedNum: Int, totalPages: Int): List[(String, Int)] = {
    val pl = proxyIPWeb.par
    pl.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(30))
    val lb = new ListBuffer[(String, Int)]
    for (x <- pl) {
      breakable {
        val ipReg = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3} \\d{1,6}"
        val ipPtn = Pattern.compile(ipReg)
        for (i <- 1 to totalPages) {
          breakable {
            val doc = Jsoup.connect(x + i + "/").get()
            println(x + i + "/")
            //println(doc.text())
            val m = ipPtn.matcher(doc.text())
            while (m.find()) {
              val str = m.group().split(" ")
              if (checkProxy(str(0), str(1).toInt)) {
                lb.append((str(0), str(1).toInt))
                println(str.mkString(":"))
              }
            }
            // fixme 并发时这种break 方式不起作用
            if (lb.size >= wantedNum) break
          }
        }
        if (lb.size >= wantedNum) break
      }
    }
    lb.toList
  }

  def checkProxy(ip: String, port: Int): Boolean = {
    //Try(Jsoup.connect("http://1212.ip138.com/ic.asp")
    Try(Jsoup.connect(testUrl)
      .timeout(2 * 1000).proxy(ip, port)
      .get()) match {
      case Success(d) => true
      case _ => false
    }
  }

}
