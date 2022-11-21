package com.reactive.banker.repository;

import com.reactive.banker.entity.BankingAgg;
import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.Property;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface BankingAggRepository extends ReactiveMongoRepository<BankingAgg, String>{

    @Aggregation("  {\n" +
            "    '$match': {\n" +
            "      'aggregateId': 'agg-test101'\n" +
            "    }\n" +
            "  }, {\n" +
            "    '$project': {\n" +
            "      '_id': 0, \n" +
            "      'aggregateId': 1, \n" +
            "      'contractList': 1, \n" +
            "      'name': 1\n" +
            "    }\n" +
            "  }")
    Flux<BankingAgg> groupContractRaw();

    @Aggregation(" {'$unwind': { 'path': '$contractList', 'includeArrayIndex': 'contracts', 'preserveNullAndEmptyArrays': false} }, \n" +
            "{'$group': {'_id': '$contracts', 'contractList': { '$push': '$contractList'}  } }, \n" +
            "{ '$project': { '_id': 0,  'contracts': '$_id', 'contractList': 1 } }")
    Flux<Contract> unwindTrialOne();

    @Aggregation("{ $group: { _id : $property, contractList : { @addToSet : $contractList } } }")
    Flux<BankingAgg> groupByPropertyAndContractList();

    @Aggregation("{ $group: { _id : $property, contractList, clientList, contractList, bankerList : { $addToSet : $?0 } } }")
    Flux<BankingAgg> groupByPropertyAnd(String property);

    @Aggregation(pipeline = {
            "{'$match':{'propertyId': ?0, 'mailCode' : ?1 } }"
    })
    Flux<Property> findPropertiesByPropertyIdAndMailCode(String propertyId, String mailCode);

    @Aggregation(pipeline = {
            "{'$match':{'contractId': ?0 } }"
    })
    Flux<Contract> findPropertiesByContractId( String contractId);

    //Sorting and Paging
    @Aggregation(pipeline = {
            "{'$match':{'originalAmount':?0, 'amountDue': {$gt: ?1} } }",
            "{'$sample':{size:?2}}",
            "{'$sort':{'area':-1}}"
    })
    Flux<Property> findContractsByOriginalAmountAndAmountDueGT(Double originalAmount, Double amountDue);

}

/*https://docs.spring.io/spring-data/mongodb/docs/current-SNAPSHOT/reference/html/#mongodb.repositories.queries.aggregation
 *
 * should work
 *  @Aggregation("{ $group: { _id : $lastname, names : { $addToSet : $firstname } } }")
List<PersonAggregate> groupByLastnameAndFirstnames();

@Aggregation("{ $group: { _id : $lastname, names : { $addToSet : $firstname } } }")
List<PersonAggregate> groupByLastnameAndFirstnames(Sort sort);

@Aggregation("{ $group: { _id : $lastname, names : { $addToSet : $?0 } } }")
List<PersonAggregate> groupByLastnameAnd(String property);

@Aggregation("{ $group: { _id : $lastname, names : { $addToSet : $?0 } } }")
List<PersonAggregate> groupByLastnameAnd(String property, Pageable page);

@Aggregation("{ $group : { _id : null, total : { $sum : $age } } }")
SumValue sumAgeUsingValueWrapper();

@Aggregation("{ $group : { _id : null, total : { $sum : $age } } }")
Long sumAge();

@Aggregation("{ $group : { _id : null, total : { $sum : $age } } }")
AggregationResults<SumValue> sumAgeRaw();

@Aggregation("{ '$project': { '_id' : '$lastname' } }")
List<String> findAllLastnames();
 */