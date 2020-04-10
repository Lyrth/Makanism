@SuppressWarnings("JavaRequiresAutoModule")
module lyrth.makanism.common {

    requires transitive com.google.gson;
    requires transitive discord.json;
    requires transitive discord4j.common;
    requires transitive discord4j.core;
    requires transitive discord4j.gateway;
    requires transitive discord4j.rest;
    requires transitive discord4j.voice;
    requires transitive emoji.java;
    requires transitive io.github.classgraph;
    requires transitive lava.common;
    requires transitive lavaplayer;
    requires transitive lettuce.core;
    requires transitive org.apache.logging.log4j;
    requires transitive org.fusesource.jansi;
    requires transitive org.reactivestreams;
    requires transitive org.slf4j;
    requires transitive reactor.blockhound;
    requires transitive reactor.core;
    requires transitive reactor.extra;
    requires transitive reactor.netty;
    requires transitive stores.api;
    requires transitive stores.jdk;
    requires transitive stores.redis;

    exports lyrth.makanism.common.util;
    exports lyrth.makanism.common.util.reactor;

}