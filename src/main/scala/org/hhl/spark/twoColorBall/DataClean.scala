package org.hhl.spark.twoColorBall

import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{SaveMode, SparkSession}

import scala.io.Source

/**
  * Created by huanghl4 on 2017/11/17.
  */
object DataClean {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder.master("local[6]")
      .appName("clean")
      .getOrCreate()

    clean2ColorBall
    cleanWx(spark)
  }

  def clean2ColorBall:Map[String,List[Int]] = {
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
       val res = List(r1,r2,r3,r4,r5,r6,b7,saleroom,jackpot)
      date -> res
    }).toMap
    data
  }


  def cleanWx(spark:SparkSession) = {
    val m = clean2ColorBall
    val data = Source.fromFile("/Users/hhl/mypro/SparkAppExamples/wx.txt").getLines()
      .map(_.split(";")).filter(_.size !=1)
      .map(x=>{
       // 气温，露点，湿度，气压,风向，风速，状况 6个feature
        val date = x(0)
      val time = x(1).replace("PM","").trim
      val qw = x(2).toDouble
      val ld = x(3).toDouble
      val sd = x(4).replace("%","").toDouble
        val qy = x(5).toDouble
        val fx = x(6)
        val fs = x(7).toDouble
        val zk = x(8)
        (date,time,qw,ld,sd,qy,fx,fs,zk)
    }).filter(_._6 != -1).toList
    // 归一化，实数化
    val fxList = data.map(_._7).distinct.zipWithIndex.toMap
    val zkList = data.map(_._9).distinct.zipWithIndex.toMap
    val res = data.map(x=>(x._1,x._2,x._3,x._4,x._5,x._6,
      fxList.getOrElse(x._7,-1).toDouble,x._8,zkList.getOrElse(x._9,-1).toDouble)).groupBy(_._1)
      .map(x=>x._2.sortBy(_._2).head).toList
    val res1 = res.sortBy(x=>(x._1,x._2)).map(x=>{
      val lq = m.getOrElse(x._1,List(-1,-1,-1,-1,-1,-1,-1))(6).toDouble
      val jc = m.getOrElse(x._1,List(-1,-1,-1,-1,-1,-1,-1))(7).toDouble
      val values = Array(x._3 + 100 ,x._4 + 100,x._5,x._6,x._7,x._8,x._9,jc) // naive beyes 不支持负值
      val featureVecotr =Vectors.dense(values.init)
      val lable = lq
      (lable,featureVecotr)
    })

    import spark.implicits._
    val df = spark.createDataset(res1).repartition(1).toDF("label","features")
    df.write.mode(SaveMode.Overwrite).parquet("/Users/hhl/mypro/SparkAppExamples/res.txt")
    //df.write.format("csv").mode(SaveMode.Overwrite).save("/Users/hhl/mypro/SparkAppExamples/res.txt")
  }

}
