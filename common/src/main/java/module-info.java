import lyrth.makanism.common.file.SourceProvider;
import lyrth.makanism.common.file.impl.FileSourceProvider;

@SuppressWarnings("JavaRequiresAutoModule")
module lyrth.makanism.common {

    //exports lyrth.makanism.common;
    exports lyrth.makanism.common.file;
    exports lyrth.makanism.common.file.config;
    exports lyrth.makanism.common.file.impl;
    exports lyrth.makanism.common.reactor;

    // the replacement to META-INF/services
    provides SourceProvider
        with FileSourceProvider;


    // Export heck below.
    requires transitive com.fasterxml.jackson.annotation;   // just for the warnings
    requires transitive com.google.gson;
    requires transitive discord4j.common;
    requires transitive discord4j.core;
    requires transitive discord4j.discordjson;
    requires transitive discord4j.discordjson.api;
    requires transitive discord4j.gateway;
    requires transitive discord4j.rest;
    requires transitive discord4j.store.api;
    requires transitive discord4j.store.jdk;
    requires transitive discord4j.store.redis;
    requires transitive discord4j.voice;
    requires transitive emoji4j;
    requires transitive io.github.classgraph;
    requires transitive io.netty.codec;
    requires transitive io.netty.codec.http;
    requires transitive io.netty.handler;
    requires transitive lettuce.core;
    requires transitive org.apache.logging.log4j.impl;
    requires transitive org.fusesource.jansi;
    requires transitive org.reactivestreams;
    requires transitive org.slf4j;
    requires transitive reactor.blockhound;
    requires transitive reactor.core;
    requires transitive reactor.extra;
    requires transitive reactor.netty;
}