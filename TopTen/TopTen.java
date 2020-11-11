import java.io.*; 
import java.util.*; 
import java.util.Map; 
import java.util.TreeMap; 

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


public class TopTen{
    public static class TopTenMapper extends Mapper<Object, 
                            Text, Text, LongWritable> { 

        private TreeMap<Long, String> record; 

        @Override
        public void setup(Context context) throws IOException, 
                                        InterruptedException 
        { 
            record = new TreeMap<Long, String>(); 
        } 

        @Override
        public void map(Object key, Text value, 
            Context context) throws IOException, InterruptedException 
        { 
            String[] tokens = value.toString().split(" "); 

            String name = tokens[0]; 
            long count = Long.parseLong(tokens[1]); 

            record.put(count, name); 

            if (record.size() > 10) 
            { 
                record.remove(record.firstKey()); 
            } 
        } 

        @Override
        public void cleanup(Context context) throws IOException, 
                                        InterruptedException 
        { 
            for (Map.Entry<Long, String> entry : record.entrySet())  
            { 

                long count = entry.getKey(); 
                String name = entry.getValue(); 

                context.write(new Text(name), new LongWritable(count)); 
            } 
        } 
    } 

    public static class TopTenReducer extends Reducer<Text, 
                    LongWritable, LongWritable, Text> { 
        private TreeMap<Long, String> record; 
    
        @Override
        public void setup(Context context) throws IOException, InterruptedException 
        { 
            record = new TreeMap<Long, String>(); 
        } 
    
        @Override
        public void reduce(Text key, Iterable<LongWritable> values, 
        Context context) throws IOException, InterruptedException 
        { 
    
            String name = key.toString(); 
            long count = 0; 
    
            for (LongWritable val : values) 
            { 
                count = val.get(); 
            } 
    
            record.put(count, name); 
    
            if (record.size() > 10) 
            { 
                record.remove(record.firstKey()); 
            } 
        } 
    
        @Override
        public void cleanup(Context context) throws IOException, InterruptedException 
        { 
            for (Map.Entry<Long, String> entry : record.entrySet())  
            { 
    
                long count = entry.getKey(); 
                String name = entry.getValue(); 
                context.write(new LongWritable(count), new Text(name)); 
            } 
        } 
    } 

    public static void main(String[] args) throws Exception 
    { 
        Configuration conf = new Configuration(); 

        Job job = Job.getInstance(conf, "topten"); 
        
        job.setJarByClass(TopTen.class); 
        job.setMapperClass(TopTenMapper.class); 
        job.setReducerClass(TopTenReducer.class); 

        job.setMapOutputKeyClass(Text.class); 
        job.setMapOutputValueClass(LongWritable.class); 

        job.setOutputKeyClass(LongWritable.class); 
        job.setOutputValueClass(Text.class); 

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1); 
    } 
}