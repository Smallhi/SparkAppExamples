package org.hhl.spark.ip

import com.snowplowanalytics.maxmind.iplookups.IpLookups
import org.apache.spark.SparkFiles
import org.apache.spark.sql.SparkSession

/**
  * Created by huanghl4 on 2017/8/27
  *
  */
object SparkTest {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName(s"${this.getClass.getSimpleName}")
      .enableHiveSupport()
      .getOrCreate()

    val sc = spark.sparkContext

    val path = "hdfs://namenodeha/user/hhl/GeoCity.dat"
    //广播文件到每一个executor
    sc.addFile(path)
    val sql = s"select d_id,preserve1 from hive.rawIP"
    import spark.implicits._
    val ips = spark.sql(sql).map(x => (x.getString(0), x.getString(1))).filter(_._2 != "")
    val l = ips.map(x => {
      // IpLookups 类 使用new File（）获取本地文件，所以只能在executor 上获取字典文件
      val geoFile = SparkFiles.get("GeoCity.dat")
      val ipLookups = IpLookups(Some(geoFile), Some(geoFile),
        Some(geoFile), Some(geoFile), Some(geoFile), true, 100)
      var locValue = (x._1, x._2, "", "", "")
      for (loc <- ipLookups.performLookups(x._2)._1) {
        val ip = x._2
        val id = x._1
        val cty = loc.countryName
        val region = loc.region.getOrElse("")
        val ct = loc.city.getOrElse("")
        locValue = (id, ip, cty, region, ct)
      }
      locValue
    }).toDF("id", "ip", "country", "region", "city")
    l.write.mode("overwrite").saveAsTable("IP_city")
  }


}
