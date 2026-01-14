package titanic

object NaiveBayes {

  def countAttributeValues(data:List[Map[String, Any]], attribList:String): Map[Any,Int]=
    data
      //Map("on time" -> ...., "late" -> ...)
      .groupBy(_(attribList)) //Map[String, List[Map[String, String]]]
      .map((wert, eintr) => (wert, eintr.size)) //Map[String, Int]


  def getAttributes(data:List[Map[String, Any]]):Set[String]=
    data.flatMap(_.keys).toSet


  def getAttributeValues(data:List[Map[String, Any]]):Map[String,Set[Any]]={

    val attribs= getAttributes(data)
    attribs.map(a => (a,data.map(_(a)).groupBy(identity).keys.toSet)).
      toMap
  }


  def calcPriorPropabilities(data:List[Map[String, Any]], classAttrib:String):Map[Any,Double]=
    val total = data.size.toDouble
    countAttributeValues(data, classAttrib).map((name, value) => (name, value / total))


  def calcAttribValuesForEachClass(data:List[Map[String, Any]], classAttrib:String):
  Map[Any, Set[(String, Map[Any, Int])]] =
    val grouped = data.groupBy(_(classAttrib))
    grouped.map((cat, entr) => {
        val attributes = getAttributes(entr) - classAttrib
        val attributCount = attributes.map { attr =>
          attr -> countAttributeValues(entr, attr)
        }.toSet
        cat -> attributCount
      })


  def calcConditionalPropabilitiesForEachClass(data: Map[Any, Set[(String, Map[Any, Int])]],classCounts:Map[Any,Int]):
  Map[Any,Set[(String, Map[Any, Double])]] =

    data.map ((cat, attr) =>
      val total = classCounts(cat).toDouble
      val prob = attr.map ((attName, attMap) =>
        attName -> attMap.map((v, count) => v -> (count / total))
      )
      cat -> prob
    )


  def calcClassValuesForPrediction(record:Map[String,Any], conditionalProps: Map[Any,Set[(String, Map[Any, Double])]],
                                   priorProps:Map[Any,Double]):Map[Any,Double]=

    conditionalProps.map {(value, entrSet) =>
      val prob = entrSet.foldLeft(priorProps(value)) { //P(c)
        case (acc, (attr, attrMap))  =>
          acc * attrMap.getOrElse(record(attr), 0.0) //produkt(P(d|c))
      }
      value -> prob
    }


  def findBestFittingClass(classProps:Map[Any,Double]):Any=
    classProps.maxBy(_._2)._1


  def modelForTrainExample(trainDataSet: List[Map[String, Any]], classAttrib:String):
  (Map[String, Any], String) => (Any, Any) ={

    val classVals= NaiveBayes.countAttributeValues(trainDataSet,classAttrib)
    val data= NaiveBayes.calcAttribValuesForEachClass(trainDataSet,classAttrib)
    val condProp = NaiveBayes.calcConditionalPropabilitiesForEachClass(data,classVals)
    val prior= NaiveBayes.calcPriorPropabilities(trainDataSet,classAttrib)
    (map,id_key) => (map(id_key),findBestFittingClass(NaiveBayes.calcClassValuesForPrediction(map-id_key,condProp,prior)))
  }


  def applyModel[CLASS, ID](model: (Map[String, Any], String) => (ID, CLASS),
                            testdata: Seq[Map[String, Any]], idKey: String): Seq[(ID, CLASS)] = {

    testdata.map(d => model(d, idKey))
  }


  def calcConditionalPropabilitiesForEachClassWithSmoothing
  (data: Map[Any, Set[(String, Map[Any, Int])]],  attValues:Map[String,Set[Any]],
   classCounts:Map[Any,Int]):
  Map[Any,Set[(String, Map[Any, Double])]] =
    //(#(d n c) + 1)  /  (#(c) + m) instead of #(dnc)/#(c)
    data.map { (cat, entr) =>
      val c = classCounts(cat).toDouble

      val prob = entr.map { (attr, attrMap) =>
        val values = attValues(attr)
        val m = values.size

        val probs = values.map{ v=>
          val count = attrMap.getOrElse(v, 0)
          v -> ((count+1).toDouble / (c+m))
        }.toMap
        attr -> probs
      }
      cat -> prob
    }




  def modelwithAddOneSmoothing(trainDataSet: List[Map[String, Any]], classAttrib:String):
  (Map[String, Any], String) => (Any, Any) ={

    val classVals= NaiveBayes.countAttributeValues(trainDataSet,classAttrib)
    val aValues = getAttributeValues(trainDataSet).asInstanceOf[ Map[String, Set[Any]]]
    val data: Map[Any, Set[(String, Map[Any, Int])]] = NaiveBayes.calcAttribValuesForEachClass(trainDataSet,classAttrib)
    val condProp =calcConditionalPropabilitiesForEachClassWithSmoothing(data,aValues,classVals)
    val prior= NaiveBayes.calcPriorPropabilities(trainDataSet,classAttrib)
    (map,id_key) => (map(id_key),findBestFittingClass(NaiveBayes.calcClassValuesForPrediction(map-id_key,condProp,prior)))
  }

  /*****************************************************************************
   *                            Utils                                          *
   *   helper Functions used by the tests                                      *
   *****************************************************************************/

  def extractValues(data: Map[Any, Set[(String, Map[Any, Any])]]):Set[(Any,Any,Any)]={

    val x= for ((key,classVals) <- data ; (att,attVals) <-classVals ; (k,v)<-attVals) yield(
      key,k,v
    )
    x.toSet
  }
}