package org.hhl.spark.twoColorBall
import scala.io.Source

/**
  * Created by huanghl4 on 2017/11/17.
  */
object DataClean {

  def main(args: Array[String]): Unit = {
  clean2ColorBall
  }

  def clean2ColorBall = {
    val data = Source.fromFile("/Users/hhl/mypro/SparkAppExamples/Hello.txt").getLines().filter(!_.contains("16115期"))
      .map(_.split(";")).map(x=>{
      val qh = x(0).substring(0,7)
      val date = x(1).replace("-","")
      val r1 = x(2).toInt
      val r2 = x(3).toInt
      val r3= x(4).toInt
      val r4 = x(5).toInt
      val r5 = x(6).toInt
      val r6 = x(7).toInt
      val b7 = x(8).toInt
      val saleroom = x(9).replace("元","").replace(",","").trim.toInt
      val jackpot = x(10).replace("元","").replace(",","").trim.toInt
      (qh,date,r1,r2,r3,r4,r5,r6,b7,saleroom,jackpot)
    })

    data.foreach(println)
  }

}
