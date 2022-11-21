package com.reactive.banker;

import com.reactive.banker.entity.BankingAgg;
import com.reactive.banker.entity.Contract;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@SpringBootTest
@EnableMongoRepositories
@Slf4j
public class TemplateAggregationTest {

    @Autowired
    ReactiveMongoTemplate template;

    @Test
    @Order(value = 1)
    @Disabled
    public void testUnwindThroughTemplate(){

        /*
        (" {'$unwind': { 'path': '$contractList', 'includeArrayIndex': 'contracts', 'preserveNullAndEmptyArrays': false} }, \n" +
            "{'$group': {'_id': '$contracts', 'contractList': { '$push': '$contractList'}  } }, \n" +
            "{ '$project': { '_id': 0,  'contracts': '$_id', 'contractList': 1 } }")
         */


        UnwindOperation unwind = Aggregation.unwind("contractList");
        GroupOperation groupOperation = Aggregation.group("contractList").push("contractId").as("contracts");
        ProjectionOperation projectionOperation = Aggregation.project("contracts").and("_id").as("contract");

        assertNotNull(unwind.getFields());
        assertNotNull(groupOperation.getFields());
        assertNotNull(projectionOperation.getOperator());

        TypedAggregation<BankingAgg> typedAggregation = Aggregation.newAggregation(
                BankingAgg.class,
                unwind,
                groupOperation,
                projectionOperation
        );

        Flux<Contract> mongoContract =  template.aggregate(typedAggregation, Contract.class);
        mongoContract
                .doOnNext(c -> System.out.println("c to string " + c.toString()))
                .subscribe();

    }
}
