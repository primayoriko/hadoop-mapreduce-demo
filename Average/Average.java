import java.io.*; 
import java.util.*; 
// import java.util.Map; 
// import java.util.TreeMap; 

import org.apache.hadoop.io.Text; 
import org.apache.hadoop.io.LongWritable; 
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer; 

import org.apache.hadoop.conf.Configuration; 
import org.apache.hadoop.fs.Path; 
import org.apache.hadoop.mapreduce.Job; 
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat; 
import org.apache.hadoop.util.GenericOptionsParser;


public class Average{
    public static class AverageMapper extends Mapper<Object, 
                            Text, Text, LongWritable> { 
        private LongWritable result = new LongWritable();
        
        @Override
        public void map(Object key, Text value, 
            Context context) throws IOException, InterruptedException 
        { 
            String[] tokens = value.toString().split(" "); 

            String name = tokens[0]; 
            long val = Long.parseLong(tokens[1]); 

            result.set(val);
            context.write(new Text(name), result);
        } 
    } 

    public static class AverageReducer extends Reducer<Text, 
                    LongWritable, Text, LongWritable> { 
        private LongWritable result = new LongWritable();
        
        @Override
        public void reduce(Text key, Iterable<LongWritable> values, 
        Context context) throws IOException, InterruptedException 
        { 
            String name = key.toString(); 
            long sum = 0, count = 0; 
    
            for (LongWritable val : values) 
            { 
                sum += val.get();
                count += 1;
            } 

            result.set(sum/count);
            context.write(key, result);
        } 
    } 

    public static void main(String[] args) throws Exception 
    { 
        Configuration conf = new Configuration(); 

        Job job = Job.getInstance(conf, "Average"); 
        
        job.setJarByClass(Average.class); 
        job.setMapperClass(AverageMapper.class); 
        job.setReducerClass(AverageReducer.class); 

        job.setMapOutputKeyClass(Text.class); 
        job.setMapOutputValueClass(LongWritable.class); 

        job.setOutputKeyClass(LongWritable.class); 
        job.setOutputValueClass(Text.class); 

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1); 
    } 
}