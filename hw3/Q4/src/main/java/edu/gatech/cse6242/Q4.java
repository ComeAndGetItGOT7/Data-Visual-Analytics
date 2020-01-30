package edu.gatech.cse6242;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;

public class Q4 
{
public static class DiffMapper extends Mapper<Object, Text, Text, IntWritable>
    {

      private final static IntWritable out = new IntWritable(1);
      private final static IntWritable in = new IntWritable(-1);
      private Text word = new Text();
      private Text word2 = new Text();

      public void map(Object key, Text value, Context context) 
			throws IOException, InterruptedException 
        {  
          StringTokenizer itr = new StringTokenizer(value.toString());
          int tag = 0;
          while (itr.hasMoreTokens()) 
            {
              if (tag == 0){
				//set out-degree for source
                word.set(itr.nextToken());
                context.write(word, out);
              }
              else if (tag == 1){   
				//set in-degree for source			  
                word2.set(itr.nextToken());
                context.write(word2, in);
              }
              else{
                itr.nextToken();
              }
              tag = tag + 1;
            }
        }
    }

  public static class DiffReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
      private IntWritable result = new IntWritable();

      public void reduce(Text key, Iterable<IntWritable> values, Context context) 
				throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable val : values){
			sum = sum + val.get();
        }
        result.set(sum);
        context.write(key, result);
      }
  }

  public static class CountMapper extends Mapper<Object, Text, Text, IntWritable>{

      private final static IntWritable one = new IntWritable(1);
      private Text word = new Text();
     
      public void map(Object key, Text value, Context context) 
      throws IOException, InterruptedException{  
          String[] lines = value.toString().split("\t");
            if(lines.length == 2){
                word.set(lines[1]);
                context.write(word, one);
            }
        }
    }

    public static class CountReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
      private IntWritable result = new IntWritable();

		  public void reduce(Text key, Iterable<IntWritable> values, Context context) 
					throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values){
			  sum = sum + val.get();
			}
			result.set(sum);
			context.write(key, result);
		  }
	}

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Q4-1");

    /* TODO: Needs to be implemented */
    job.setJarByClass(Q4.class);
    job.setMapperClass(DiffMapper.class);
    job.setCombinerClass(DiffReducer.class);
    job.setReducerClass(DiffReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path("tmp"));

    job.waitForCompletion(true);
	
	
    Job job2 = Job.getInstance(conf, "Q4-2");
    job2.setJarByClass(Q4.class);
    job2.setMapperClass(CountMapper.class);
    job2.setCombinerClass(CountReducer.class);
    job2.setReducerClass(CountReducer.class);
    job2.setOutputKeyClass(Text.class);
    job2.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job2, new Path("tmp"));
    FileOutputFormat.setOutputPath(job2, new Path(args[1]));

    System.exit(job2.waitForCompletion(true) ? 0 : 1);
  }
}