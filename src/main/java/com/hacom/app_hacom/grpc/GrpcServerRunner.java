package com.hacom.app_hacom.grpc;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcServerRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerRunner.class);

    private final OrderGrpcService orderGrpcService;
    private Server server;

    public GrpcServerRunner(OrderGrpcService orderGrpcService) {
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
        int port = 6565;
        server = NettyServerBuilder.forPort(port)
                .addService(orderGrpcService)
                // enable server reflection so tools like grpcurl can discover services and methods
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();
        log.info("gRPC server started on port {}", port);
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            log.info("gRPC server stopped");
        }
    }
}
