import titanic.CreatePrediction.{test, testDataset, trainDataset}
import titanic.{TitanicDataSet, Utils}

// load datsets
val train = Utils.loadDataCSV("train.csv")
val test = Utils.loadDataCSV("test.csv")
val all = train ++ test
val trainDataset = TitanicDataSet.createDataSetForTraining(train)
val testDataset = TitanicDataSet.createDataSetForTraining(test)

val attrList = List("sex", "pclass", "survived", "fare", "age", "survived")
val attList = List("passengerID", "pclass", "survived", "name", "sex", "age", "sibsp", "parch",
  "ticket", "fare", "cabin", "embarked")

TitanicDataSet.countAllMissingValues(trainDataset, attrList)
TitanicDataSet.countAllMissingValues(testDataset, attrList)

titanic.NaiveBayes.countAttributeValues(testDataset, "fare")
titanic.NaiveBayes.getAttributeValues(testDataset)

val model = TitanicDataSet.createModelWithTitanicTrainingData(trainDataset, "survived")
val predictions = titanic.NaiveBayes.applyModel(model, testDataset, "passengerID")
