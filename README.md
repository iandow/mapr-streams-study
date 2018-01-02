# MapR Streams Study

This project includes a few examples that show how to work with streaming data using MapR Streams.

## Pre-requisites

You will need maven and a MapR cluster or the MapR developer sandbox container for MapR version 6.0 or later.

### Step 1: Create the stream and topic

A *stream* is a collection of topics that you can manage together for security, default number or partitions, and time to leave for the messages.

Run the following command on your MapR cluster:

```
$ maprcli stream create -path /apps/mystream -produceperm p -consumeperm p -topicperm p -ttl 900
$ maprcli stream topic create -path /apps/mystream -topic mytopic -partitions 3
```

Now, you can list the topic like this to make sure it was created:

```
$ maprcli stream topic list -path /apps/mystream
topic            partitions  logicalsize  consumers  maxlag  physicalsize
trades           1           0            0          0       0
```

### Step 2: Start consumer

Copy the jar file and resources folder to a cluster node then you can run the consumer and producer as shown below.

Start the consumer like this. Be sure to include the resources folder in your classpath (as shown in this command) so the consumer.props file can be loaded.

```
java -cp src/resources/:target/mapr-streams-study-1.0-jar-with-dependencies.jar com.mapr.examples.Run consumer /user/mapr/ianstudy:mytopic
```

### Step 2: Start producer

Start the producer like this. Be sure to include the resources folder in your classpath (as shown in this command) so the producer.props file can be loaded.

```
java -cp src/resources/:target/mapr-streams-study-1.0-jar-with-dependencies.jar com.mapr.examples.Run producer /user/mapr/ianstudy:mytopic input_file.txt
```

### Step 3: Create the MapR-DB table

Create the table that will be used to persist parsed TAQ records consumed off the stream.

```
$ maprcli table info -path /apps/mytable
```


### Monitoring your topics 

You can use the `maprcli` tool to get some information about the topic, for example:

```
$ maprcli stream info -path /user/mapr/taq -json
$ maprcli stream topic info -path /user/mapr/taq -topic trades -json
```


## Cleaning Up

When you are done, you can delete the stream, and all associated topic using the following command:
```
$ maprcli stream delete -path /taq
```
