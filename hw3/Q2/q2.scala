// Databricks notebook source
// Q2 [25 pts]: Analyzing a Large Graph with Spark/Scala on Databricks

// STARTER CODE - DO NOT EDIT THIS CELL
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import spark.implicits._

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
// Definfing the data schema
val customSchema = StructType(Array(StructField("answerer", IntegerType, true), StructField("questioner", IntegerType, true),
    StructField("timestamp", LongType, true)))

// COMMAND ----------

// STARTER CODE - YOU CAN LOAD ANY FILE WITH A SIMILAR SYNTAX.
// MAKE SURE THAT YOU REPLACE THE examplegraph.csv WITH THE mathoverflow.csv FILE BEFORE SUBMISSION.
val df = spark.read
   .format("com.databricks.spark.csv")
   .option("header", "false") // Use first line of all files as header
   .option("nullValue", "null")
   .schema(customSchema)
   .load("/FileStore/tables/mathoverflow.csv")
   .withColumn("date", from_unixtime($"timestamp"))
   .drop($"timestamp")

// COMMAND ----------

//display(df)
df.show()

// COMMAND ----------

// PART 1: Remove the pairs where the questioner and the answerer are the same person.
// ALL THE SUBSEQUENT OPERATIONS MUST BE PERFORMED ON THIS FILTERED DATA

// ENTER THE CODE BELOW
val filtered_df = df.filter("answerer != questioner")
filtered_df.show()

// COMMAND ----------

// PART 2: The top-3 individuals who answered the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest out-degrees.

// ENTER THE CODE BELOW
val answer_most = filtered_df.groupBy("answerer").agg(count(filtered_df("questioner"))).withColumnRenamed("count(questioner)","questions_answered")
val sorted_answer_most = answer_most.sort($"questions_answered".desc,$"answerer").limit(3)
sorted_answer_most.show()

// COMMAND ----------

// PART 3: The top-3 individuals who asked the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest in-degree.

// ENTER THE CODE BELOW
val ask_most = filtered_df.groupBy("questioner").agg(count(filtered_df("answerer"))).withColumnRenamed("count(answerer)","questions_asked")
val sorted_ask_most = ask_most.sort($"questions_asked".desc,$"questioner").limit(3)
sorted_ask_most.show()

// COMMAND ----------

// PART 4: The top-5 most common asker-answerer pairs - sorted in descending order - if tie, the one with lower value node-id in the first column (u->v edge, u value) gets listed first.

// ENTER THE CODE BELOW
val most_common_pairs = filtered_df.groupBy( $"answerer",$"questioner").agg(count(filtered_df("answerer"))).withColumnRenamed("count(answerer)","count")
val sorted_most_common_pairs = most_common_pairs.sort($"count".desc,$"answerer",$"questioner").limit(5)
sorted_most_common_pairs.show()

// COMMAND ----------

// PART 5: Number of interactions (questions asked/answered) over the months of September-2010 to December-2010 (i.e. from September 1, 2010 to December 31, 2010). List the entries by month from September to December.

// Reference: https://www.obstkel.com/blog/spark-sql-date-functions
// Read in the data and extract the month and year from the date column.
// Hint: Check how we extracted the date from the timestamp.

// ENTER THE CODE BELOW
val filtered_df_ym_extracted = filtered_df
.withColumn("year",year(to_timestamp(filtered_df("date"))))
.withColumn("month",month(to_timestamp(filtered_df("date"))))

val num_of_interactions = filtered_df_ym_extracted
.filter("year = 2010 and month >= 9 and month <= 12")
.groupBy("month")
.agg(count("date"))
.withColumnRenamed("count(date)","total_interactions")
.sort($"month".desc)
num_of_interactions.show()                                              

// COMMAND ----------

// PART 6: List the top-3 individuals with the maximum overall activity, i.e. total questions asked and questions answered.

// ENTER THE CODE BELOW
val overall_activitiy = answer_most
.join(ask_most, $"answerer" === $"questioner")
.withColumnRenamed("answerer", "userID")
.withColumn("total_activity",$"questions_answered"+$"questions_asked")
.drop($"questioner")
.drop($"questions_answered")
.drop($"questions_asked")
.sort($"total_activity".desc, $"userID")
.limit(3)
overall_activitiy.show()
