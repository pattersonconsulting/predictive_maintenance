
import com.snowflake.snowpark._
import com.snowflake.snowpark.functions._
import com.snowflake.snowpark.types._

import org.pmml4s.model.Model
import org.pmml4s.data.Series

import spray.json._

import scala.io.Source

object example_pmml_predict {

  def main(args: Array[String]): Unit = {

    val libPath = new java.io.File("").getAbsolutePath
    println(libPath)

    val model: Model = Model.fromFile( s"$libPath/lib/pmml/pm_sklearn_xgb.pmml")

    val inputNames = model.inputNames

    println("Input Names: " + inputNames.mkString(" "))

    val result = model.predict(Map("TYPE" -> 1, "AIR_TEMPERATURE" -> 298.1, "PROCESS_TEMPERATURE" -> 408.6, "ROTATIONAL_SPEED" -> 1551, "TORQUE" -> 52.8, "TOOL_WEAR" -> 500))

    val failurePredictionScore = result.get("probability(1)").get.asInstanceOf[Double]
    println("failurePredictionScore: " + failurePredictionScore)
    
  }
}

