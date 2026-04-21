package com.hacom.app_hacom.akka;

import com.hacom.app_hacom.grpc.InsertOrderRequest;
import com.hacom.app_hacom.grpc.InsertOrderResponse;
import io.grpc.stub.StreamObserver;

public class ProcessOrderMessage {
    private final InsertOrderRequest request;
    private final StreamObserver<InsertOrderResponse> responseObserver;

    public ProcessOrderMessage(InsertOrderRequest request, StreamObserver<InsertOrderResponse> responseObserver) {
        this.request = request;
        this.responseObserver = responseObserver;
    }

    public InsertOrderRequest getRequest() {
        return request;
    }

    public StreamObserver<InsertOrderResponse> getResponseObserver() {
        return responseObserver;
    }
}

