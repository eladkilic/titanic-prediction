package mapreduce

import java.text.SimpleDateFormat
import java.util.Locale

object MapReduce :

  val dateFormatPattern= "yyyy.MM.dd G 'at' HH:mm:ss z"
  val sdf= new SimpleDateFormat(dateFormatPattern,Locale.ENGLISH)

  /*
    (<Datum und Uhrzeit>,<Nutzer>,<Jobname>,<Dauer in Sekunden>)
    mit den Typen
    (DateFormat,String,String,Int)
  */
  def numberOfJobsPerUser(l: List[((Int, (String, String, String, Int)))]): List[(String, Int)] = {
    BasicOperations.mapReduce[
      Int, // KeyIn -index, ID
      (String, String, String, Int), // ValueIn -eigentliche datentupel
      String, // KeyMOut -benutzername
      Int, // ValueMOut -immer 1
      String, // KeyROut
      Int // ValueROut
    ](
      // mapFun: input ist ein Tuple (key, value)
      kv => List((kv._2._2, 1)), // kv._2 ist das (String, String, String, Int) Tuple
      // redFun: input ist (key, List[values])
      kv => List((kv._1, kv._2.sum)),
      l
    )
  }

  /*
  Write a function that determines how many times a job name was used from each user
  Result: Map (key:(user,Job), value: number)
  */
  def numberOfJobsPerUserUsingACertainName(l: List[(Int, (String, String, String, Int))]): List[((String, String), Int)] = {
    BasicOperations.mapReduce[
      Int, // KeyIn
      (String, String, String, Int), // ValueIn
      (String, String), // KeyMOut
      Int, // ValueMOut
      (String, String), // KeyROut
      Int // ValueROut
    ](
      kv => List(((kv._2._2, kv._2._3), 1)), // (user, job)
      kv => List((kv._1, kv._2.sum)),
      l
    )
  }

  /*
    Write a function that determines all job names (without duplicates)
    Result: List(jobnames)
  */
  def distinctNamesOfJobs(l: List[(Int, (String, String, String, Int))]): List[String] = {
    BasicOperations.mapReduce[
      Int, // KeyIn
      (String, String, String, Int), // ValueIn
      String, // KeyMOut
      Int, // ValueMOut
      String, // KeyROut
      Unit // ValueROut -für Distinct
    ](
      kv => List((kv._2._3, 1)), // jobname
      kv => List((kv._1, ())), // duplikate entfernen
      l
    ).map(_._1)
  }


  /*
    Write a function that determines how many jobs lasted more than 20sec
    Result: Map (key:("more" or "less"), value: number)
  */
  def moreThan20Seconds(l: List[(Int, (String, String, String, Int))]): List[(String, Int)] = {
    BasicOperations.mapReduce[
      Int, // KeyIn
      (String, String, String, Int), // ValueIn
      String, // KeyMOut
      Int, // ValueMOut
      String, // KeyROut
      Int // ValueROut
    ](
      kv =>
        val category = if kv._2._4 > 20 then "more" else "less"
        List((category, 1)),
      kv => List((kv._1, kv._2.sum)),
      l
    )
  }

  /*
    Write a function that determines the number of were submitted per day
    Result: Map (key:day- format "YYYY-MM-dd" , value: number)
  */
  def numberOfJobsPerDay(l: List[(Int, (String, String, String, Int))]): List[(String, Int)] = {
    val outDF= new SimpleDateFormat("YYYY-MM-dd")
    BasicOperations.mapReduce[
      Int, // KeyIn
      (String, String, String, Int), // ValueIn
      String, // KeyMOut
      Int, // ValueMOut
      String, // KeyROut
      Int // ValueROut
    ](
      kv =>
        val date = sdf.parse(kv._2._1) // kv._2 -> datentupel
        val formatted = outDF.format(date)
        List((formatted, 1)),
      kv => List((kv._1, kv._2.sum)),
      l
    )
  }


