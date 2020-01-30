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

public class Q1 {
	
	public static class TokenizerMapper
       extends Mapper<Object, Text, IntWritable, Text>{

    private IntWritable src = new IntWritable();
    private Text t_w = new Text();

	@Override
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString(), "\n");
      while (itr.hasMoreTokens()) {
        String line = itr.nextToken();
        String tokens[] = line.split("\t");
		if (tokens[1] == "" || tokens[1] == null || tokens[2] == "" || tokens[2] == null)
			continue;
		
		src.set(Integer.parseInt(tokens[0]));
        t_w.set(tokens[1] + "," + tokens[2]);
        context.write(src, t_w);
      }
    }
  }

  public static class IntSumReducer
       extends Reducer<IntWritable,Text,IntWritable,Text> {
    private Text result = new Text();

	@Override
    public void reduce(IntWritable key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {
      String max_weight = "-1";
	  String tgt = "-1";
      for (Text val : values) {
		String[] value = val.toString().split(",");
        if(Integer.parseInt(value[1]) > Integer.parseInt(max_weight)){ 
			max_weight = new String(value[1]);
			tgt = new String(value[0]);
		}
		else if (Integer.parseInt(value[1]) == Integer.parseInt(max_weight)){
			if (Integer.parseInt(value[0]) < Integer.parseInt(tgt)){
				max_weight = new String(value[1]);
				tgt = new String(value[0]);
			}
		}
	  }
      result.set(tgt + "," + max_weight);
      context.write(key, result);
    }
  }
  

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Q1");

    /* TODO: Needs to be implemented */
	job.setJarByClass(Q1.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(IntWritable.class);
    job.setOutputValueClass(Text.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
