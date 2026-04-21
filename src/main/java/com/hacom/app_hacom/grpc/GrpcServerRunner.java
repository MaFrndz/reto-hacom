package com.hacom.app_hacom.grpc;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcServerRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerRunner.class);

    @Value("${grpc.server.port:6565}")
    private int grpcPort;

    private final OrderGrpcServiceImpl orderGrpcService;
    private Server server;

    public GrpcServerRunner(OrderGrpcServiceImpl orderGrpcService) {
        this.orderGrpcService = orderGrpcService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                GrpcServerRunner.this.stop();
            } catch (InterruptedException e) {
                log.error("Interrupted during gRPC server shutdown", e);
            }
        }));
    }

    public void start() throws IOException {
        server = NettyServerBuilder.forPort(grpcPort)
                .addService(orderGrpcService)
                // enable server reflection so tools like grpcurl can discover services and methods
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();
        log.info("gRPC server started on port {}", grpcPort);
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            log.info("gRPC server stopped");
        }
    }
}
