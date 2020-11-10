import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


// javac -classpath "D:\ProgramData\Hadoop\hadoop-3.3.0\share\hadoop\common\*";"D:\ProgramData\Hadoop\hadoop-3.3.0\share\hadoop\mapreduce\*" -d WordCount/ WordCount.java
// jar -cvf WordCount.jar -C WordCount/ .
// hadoop jar WordCount.jar WordCount /input2 /output

public class WordCount {
    public static class WordMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
            private Text word = new Text();

            public void map(LongWritable key, Text value, Context context) 
                    throws IOException, InterruptedException {
                String[] stringArr = value.toString().split("\\s+");
                for (String str : stringArr) {
                    word.set(str);
                    context.write(word, new IntWritable(1));
                }           
            }
    }

    // Reduce function
    public static class CountReducer extends Reducer<Text, IntWritable, Text, IntWritable>{        
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) 
                throws IOException, InterruptedException {
            int sum = 0;

            for (IntWritable val : values)
                sum += val.get();

            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args)  throws Exception{
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "WC");

        job.setJarByClass(WordCount.class);
        job.setMapperClass(WordMapper.class);    
        job.setReducerClass(CountReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}