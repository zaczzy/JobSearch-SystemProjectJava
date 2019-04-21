import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Indexer {
    public static class IndexerMapper extends Mapper<Object, Text, Text, Text> {

        @Override
        public void map(Object key, Text doc, Context context) throws IOException, InterruptedException {

        }
    }

    public static class IndexerReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text word, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = new Job(conf, "indexer");
        job.setJarByClass(Indexer.class);
        job.setMapperClass(IndexerMapper.class);
        job.setCombinerClass(IndexerReducer.class);
        job.setReducerClass(IndexerReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //TODO: input / output path

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}