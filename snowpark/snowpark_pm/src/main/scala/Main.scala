
import com.snowflake.snowpark._
import com.snowflake.snowpark.functions._
import com.snowflake.snowpark.types._

import org.pmml4s.model.Model
import org.pmml4s.data.Series

import spray.json._

import scala.io.Source

object Main {

  object UDFCode extends Serializable {

    lazy val udf_model = {

      import java.io._

      val resourceName = "/pmml/pm_sklearn_xgb.pmml" // this is the path of the pmml in the .jar
      val inputStream = classOf[com.snowflake.snowpark.DataFrame].getResourceAsStream(resourceName)
      Model.fromInputStream(inputStream)

    }

    val testUDF_Func = (score: Double) => {

      val i = udf_model.inputNames

      val a = 0

      score
      
    }

    val predictFailureScoreUDF = (TYPE_col: String, AIR_TEMPERATURE: Double, PROCESS_TEMPERATURE: Double, ROTATIONAL_SPEED: Double, TORQUE: Double, TOOL_WEAR: Double) => {
      udf_model.predict(Map("TYPE" -> TYPE_col, "AIR_TEMPERATURE" -> AIR_TEMPERATURE, "PROCESS_TEMPERATURE" -> PROCESS_TEMPERATURE, "ROTATIONAL_SPEED" -> ROTATIONAL_SPEED, "TORQUE" -> TORQUE, "TOOL_WEAR" -> TOOL_WEAR)).get("probability(1)").get.asInstanceOf[Double]
    }

  }


  def main(args: Array[String]): Unit = {
    
    Console.println("\n=== Creating the session ===\n")

    val session = Session.builder.configFile("conn.properties").create

    val df_raw_device_data_test = session.table("SUMMARY_SENSOR_DATA_TEST") //.select(col())
    df_raw_device_data_test.show()

    val libPath = new java.io.File("").getAbsolutePath
    println(libPath)
    
    session.addDependency(s"$libPath/lib/pmml4s_2.12-0.9.11.jar")
    session.addDependency(s"$libPath/lib/spray-json_2.12-1.3.5.jar")
    session.addDependency(s"$libPath/lib/scala-xml_2.12-1.2.0.jar")
    // have to wrap pmml in a jar for this to work
    session.addDependency(s"$libPath/lib/pm_pmml_model.jar")

    session.sql("drop function if exists model_PM_UDF_predict(DOUBLE);").show()
    session.udf.registerTemporary("model_PM_UDF_predict", UDFCode.predictFailureScoreUDF)    
    session.sql("SHOW USER FUNCTIONS;").show()

    val df_machines_scored = df_raw_device_data_test.withColumn("FAILURE_SCORE", callUDF("model_PM_UDF_predict", col("TYPE"), col("AIR_TEMPERATURE"), col("PROCESS_TEMPERATURE"), col("ROTATIONAL_SPEED"), col("TORQUE"), col("TOOL_WEAR"))) 
    df_machines_scored.show()
    df_machines_scored.write.mode(SaveMode.Overwrite).saveAsTable("DAILY_SCORED_MACHINES")

    Console.println("\n=== CLOSING the session ===\n")

    session.close();

  }
}

//Main.main()