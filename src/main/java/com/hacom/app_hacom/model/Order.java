package com.hacom.app_hacom.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "orders")
public class Order {

    @Id
    private ObjectId _id;

    @Field("orderId")
    private String orderId;

    @Field("customerId")
    private String customerId;

    @Field("customerPhoneNumber")
    private String customerPhoneNumber;

    @Field("status")
    private String status;

    @Field("items")
    private List<String> items;

    @Field("ts")
    private OffsetDateTime ts;
}

