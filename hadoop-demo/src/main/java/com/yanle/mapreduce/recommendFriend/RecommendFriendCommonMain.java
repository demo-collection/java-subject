package com.yanle.mapreduce.recommendFriend;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class RecommendFriendCommonMain {

    public static void main(String[] args) throws Exception {
        //创建一个job和任务入口
        Job job = Job.getInstance(new Configuration());
        job.setJarByClass(RecommendFriendCommonMain.class);  //main方法所在的class

        //指定job的mapper和输出的类型<k2 v2>
        job.setMapperClass(RecommendFriendCommonMapper.class);
        job.setMapOutputKeyClass(Text.class);   //k2的类型
        job.setMapOutputValueClass(IntWritable.class);  //v2的类型

        //指定job的reducer和输出的类型<k4  v4>
        job.setReducerClass(RecommendFriendCommonReducer.class);
        job.setOutputKeyClass(Text.class);  //k4的类型
        job.setOutputValueClass(IntWritable.class);  //v4的类型

        //指定job的输入和输出
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        //执行job
        job.waitForCompletion(true);
    }
}