package com.mapr.examples;

import akka.actor.AbstractActor;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.io.Serializable;

public class AkkaPersister {
    public static int count = 0;
    public static class Status implements Serializable {}
    public static class ParseMe implements Serializable {
        public final String rawtext;
        public ParseMe(String rawtext) {
            this.rawtext = rawtext;
        }
    }

    public static class Parser extends AbstractActor {

        JSONObject json = new JSONObject();

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(Status.class, message -> {
                        System.out.println("Akka message count = " + count);
                    })
                    .match(ParseMe.class, message -> {
                        try {
                            json = doParse(((ParseMe) message).rawtext);
                            count++;
                        } catch (ParseException e) {
                            System.err.printf("%s", e.getStackTrace());
                            count++;
                        }
                    })
                    .matchAny(o -> System.out.println("received unknown message"))
                    .build();
        }

        private static JSONObject doParse(String rawtext) throws ParseException {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(rawtext);
            return json;
        }
    }

}
