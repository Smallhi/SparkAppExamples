package org.hhl.spark.twoColorBall

import org.jsoup.Jsoup

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.util.{Success, Try}

/**
  * Created by huanghl4 on 2017/11/20.
  */
object testProxyIP {

  def main(args: Array[String]): Unit = {
    val url = "https://www.wunderground.com/history/airport/ZBAA/2006/07/25/DailyHistory.html?req_city=Beijing&req_state=&req_statename=China&reqdb.zip=&reqdb.magic=&reqdb.wmo="
   val proxyips = List(
        "110.216.18.44:80",
        "122.72.18.35:80",
        "123.161.18.201:9797",
        "222.73.68.144:8090",
        "218.56.132.157:8080",
        "61.135.217.7:80",
        "182.34.50.90:808",
        "123.185.130.32:8118",
        "180.76.134.106:3128",
        "61.155.164.110:3128",
        "42.157.5.154:9999",
        "61.155.164.112:3128",
        "182.37.40.150:808",
        "61.155.164.109:3128",
        "36.97.145.29:9797",
        "220.249.185.178:9999",
        "122.224.227.202:3128",
        "113.200.159.155:9999",
        "183.30.204.114:9000",
        "123.139.56.238:9999",
        "114.115.140.25:3128",
        "61.160.208.222:8080",
        "116.25.100.228:9797",
        "125.46.0.62:53281",
        "112.74.94.142:3128",
        "118.31.103.7:3128",
        "139.224.24.26:8888",
        "101.37.79.125:3128",
        "163.125.21.253:9797",
        "113.200.214.164:9999",
        "61.141.186.206:9797",
        "125.45.87.12:9999",
        "122.72.18.34:80",
        "124.89.33.75:9999",
        "120.27.10.38:8090",
        "118.119.168.172:9999",
        "113.65.8.56:9999",
        "60.191.134.165:9999",
        "113.76.96.52:9797",
        "119.129.97.167:9797",
        "123.138.89.133:9999",
        "163.125.72.43:9999",
        "183.15.173.120:9999",
        "180.76.134.106:3128",
        "218.56.132.155:8080",
        "123.7.38.31:9999",
        "122.72.18.61:80",
        "61.158.111.142:53281",
        "183.30.197.219:9797",
        "218.56.132.156:8080"
      ).par
    proxyips.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(50)) // 设置并发线程数
    proxyips.foreach(x=>{
      val ip = x.split(":")(0)
      val port = x.split(":")(1)
      Try(Jsoup.connect(url)
        .timeout(2*1000).proxy(ip,port.toInt)
        .get()) match {
        case Success(d) => println(x + "is 可用的")
        case _ => println("不可用的" + x)
      }
    })



  }
}
