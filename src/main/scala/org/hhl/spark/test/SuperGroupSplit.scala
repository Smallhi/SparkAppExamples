package org.hhl.spark.test

import scala.io.Source
import java.util.UUID


/**
  * Created by huanghl4 on 2017/11/14.
  */
object SuperGroupSplit {

  def main(args: Array[String]): Unit = {

    val data = Source.fromFile("/Users/hhl/data.csv").getLines().map(_.split(",")).map(x=>(x(0),x(1),x(2),x(3))).toList
    for(e<- data) if(e._2 == "0" || e._4 == "0")  init(e)
    for(e<- data) if(e._2 != "0" && e._4 != "0")  doPut(e)



    graph.toList.foreach(println)
    // fixme 存在SID 不唯一
//    val list = graph.toList.map(x=>{
//      val key = x._1
//      val l = x._2.flatMap(t =>{
//        val src = t._1 + t._2
//        val dst = t._3 + t._4
//        List((key,src),(key,dst))
//      })
//      l
//    }).flatMap(x=>x).distinct

    //list.foreach(println)
    //list.groupBy(_._2).filter(x=>x._2.size>2).foreach(println)
  }

  type Edge = (String,String,String,String)
  type Graph = Map[String,List[Edge]]
  var graph: Graph= Map()
  def init(e:Edge) = {
    if (graph.isEmpty) graph += (treeID -> List(e))
    else {
      val sg = initSearch(e)
      put(e,sg)
    }
  }
  def doPut(e:Edge) = {
    if (graph.isEmpty) graph += (treeID -> List(e))
    else {
      // 否则，搜索，判断放在哪个List 里面
      val sg = search(e)
      put(e,sg)
    }
  }

  def treeID = UUID.randomUUID().toString().replaceAll("-", "")

  def search(e:Edge):Graph = {
    val src = e._1 + e._2
    val dst = e._3 + e._4
    graph.filter(x=> {
      val l = x._2.flatMap(x=>List(x._1 + x._2,x._3+x._4)).toSet
      if(l.contains(src) || l.contains(dst))
        true
      else false
    })
  }

  def initSearch(e:Edge):Graph = {
    val src = e._1 + e._2
    val dst = e._3 + e._4
    var v = ""
    if (e._2 == "0") v = src
    if (e._4 == "0") v = dst
    graph.filter(x=> {
      val l = x._2.flatMap(x=>List(x._1 + x._2,x._3+x._4)).toSet
      if(l.contains(v) || l.contains(v))
        true
      else false
    })
  }

  def put(e:Edge,serchedG:Graph) = {
    var sg = serchedG
    if (sg.isEmpty) graph += (treeID -> List(e))
    else {
      val tryPut = serchedG.map(x => {
        var l = x._2
        x._1 -> (l :+ e)
      })
      val key_ = tryPut.filter(x => checkSuperGroup(x._2))
      // 如果当前边在分割的图中找到了多个子图？
      val key = if (!key_.isEmpty ) key_.head._1 else ""
      //println("put" + e.toString())
      //if (key_.size >2) tryPut.foreach(println)
      graph = graph.map(x => {
        if (x._1 == key) {
          key -> (x._2 :+ e)
        } else x
      })
    }
  }

  def checkSuperGroup(tree:List[Edge]):Boolean = {
    val treeV = tree.flatMap(x=>List((x._1,x._2),(x._3,x._4))).distinct
    val emailSize = treeV.count(x=>x._2=="2")
    val lenovoEmailSize = treeV.count(x=>x._2=="2" && x._1.toLowerCase.contains("lenovo.com"))

    val lenovoidSize = treeV.count(x=>x._2 == "0")
    if (emailSize > 2 || lenovoEmailSize >1 || lenovoidSize >1) false
    else true
  }

}
