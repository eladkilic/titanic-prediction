package titanic

import titanic.NaiveBayes.findBestFittingClass

object TitanicDataSet {

  /**
   * Creates a model that predicts 1 (survived) if the person of the certain record
   * is female and 0 (deceased) otherwise
   *
   * @return The model represented as a function
   */
  def simpleModel: (Map[String, Any], String) => (Any, Any) =
    (record, idAttr) =>
      val sex = record("sex").toString.toLowerCase
      val survived = 
        if (sex=="female") 1 
        else 0
      
      (record(idAttr), survived)
      

  def countAllMissingValues(data: List[Map[String, Any]], attList: List[String]): Map[String, Int] =
    attList.flatMap(attr =>
      val count = data.count(record => !record.contains(attr))
      if (count>0) Some(attr->count) else None
    ).toMap


  def extractTrainingAttributes(record: Map[String, Any], attList: List[String]): Map[String, Any] =
    attList.filter(attr=> record.contains(attr))
      .map(attr => attr -> record(attr))
      .toMap


  def createDataSetForTraining(data: List[Map[String, Any]]): List[Map[String, Any]] =
    val attList = List("passengerID", "sex", "age", "survived", "pclass", "fare")

    val ages = data.flatMap(_.get("age") match {
      case Some(d: Double) => Some(d)
      case Some(i: Int) => Some(i.toDouble)
      case Some(f: Float) => Some(f.toDouble)
      case _ => None
    })
    val avgAge = ages.sum / ages.size

    data.map { record =>
      attList.map { attr =>
        val value: Any = attr match {
          case "age" =>
            val rawAge = record.get("age") match {
              case Some(d: Double) => d
              case Some(i: Int) => i.toDouble
              case Some(f: Float) => f.toDouble
              case _ => avgAge
            }
            ageCategory(rawAge)
          case "fare" =>
            val rawFare = record.get("fare") match {
              case Some(d: Double) => d
              case Some(i: Int) => i.toDouble
              case Some(f: Float) => f.toDouble
              case _ => 0.0
            }
            fareCategory(rawFare)
          case "name" =>
            val rawName = record.getOrElse("name", "unknown").toString
            nameLengthCategory(rawName)
          case _ =>
            record.get(attr).map(_.toString).getOrElse("unknown")
        }
        attr -> value
      }.toMap
    }


// Hilfsfunktion
def ageCategory(age: Double): String = age match {
  case a if a <= 4 => "Infant"
  case a if a <= 15 => "Child"
  case a if a <= 50 => "Adult"
  case _ => "Old"
}
def fareCategory(fare: Double): String = fare match {
  case f if f <= 7.91 => "VeryLow"
  case f if f <= 14.454 => "Low"
  case f if f <= 31.0 => "Medium"
  case f if f <= 100.0 => "High"
  case _ => "VeryHigh"
}
def nameLengthCategory(name: String): String = {
  val len = name.length
  len match {
    case l if l < 15 => "Short"
    case l if l < 25 => "Medium"
    case _ => "Long"
  }
}


  def createModelWithTitanicTrainingData(tdata: List[Map[String, Any]], classAttr: String):
  (Map[String, Any], String) => (Any, Any) =

    val dataForModel = createDataSetForTraining(tdata)

    val idData = dataForModel.map(record =>
      (record.getOrElse("passengerID", -1), record - "passengerID")
    )
    val dataForTraining = idData.map(_._2)
    val prio = NaiveBayes.calcPriorPropabilities(dataForTraining, classAttr)
    val data = NaiveBayes.calcAttribValuesForEachClass(dataForTraining, classAttr)
    val classVals = NaiveBayes.countAttributeValues(dataForTraining, classAttr)
    val condProp = NaiveBayes.calcConditionalPropabilitiesForEachClass(data, classVals)

    (record: Map[String, Any], classAttrName: String) => {
      val normalizedRecord = createDataSetForTraining(List(record)).head

      val id = normalizedRecord.getOrElse("passengerID", -1)

      // Für Vorhersage: Record ohne ID und ohne classAttr (falls vorhanden)
      val recordForPrediction = normalizedRecord - "passengerID" - classAttr

      val predictedClass = findBestFittingClass(
        NaiveBayes.calcClassValuesForPrediction(recordForPrediction, condProp, prio)
      )

      (id, predictedClass)
    }
}