// Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License"). You may
// not use this file except in compliance with the License. A copy of the
// License is located at
//
//	  http://aws.amazon.com/apache2.0/
//
// or in the "license" file accompanying this file. This file is distributed
// on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the License for the specific language governing
// permissions and limitations under the License.


package com.amazonaws.serverless.dao;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.serverless.domain.Book;
import com.amazonaws.serverless.manager.DynamoDBManager;
import org.apache.log4j.Logger;

import java.util.*;


public class DynamoDBBookDao implements BookDao {

    private static final Logger log = Logger.getLogger(DynamoDBBookDao.class);

    private static final DynamoDBMapper mapper = DynamoDBManager.mapper();

    private static volatile DynamoDBBookDao instance;


    private DynamoDBBookDao() { }

    public static DynamoDBBookDao instance() {

        if (instance == null) {
            synchronized(DynamoDBBookDao.class) {
                if (instance == null)
                    instance = new DynamoDBBookDao();
            }
        }
        return instance;
    }

    @Override
    public List<Book> findAllBooks() {
        return mapper.scan(Book.class, new DynamoDBScanExpression());
    }

    @Override
    public List<Book> findBooksByTitle(String title) {

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(title));

        DynamoDBQueryExpression<Book> query = new DynamoDBQueryExpression<Book>()
                                                    .withIndexName(Book.TITLE_INDEX)
                                                    .withConsistentRead(false)
                                                    .withKeyConditionExpression("title = :v1")
                                                    .withExpressionAttributeValues(eav);

        return mapper.query(Book.class, query);


        // NOTE:  without an index, this query would require a full table scan with a filter:
        /*
         DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                                                    .withFilterExpression("city = :val1")
                                                    .withExpressionAttributeValues(eav);

         return mapper.scan(Book.class, scanExpression);
        */

    }

    @Override
    public List<Book> findBooksByAuthor(String author) {

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(author));
        DynamoDBQueryExpression<Book> awayQuery = new DynamoDBQueryExpression<Book>()
                                                        .withIndexName(Book.AUTHOR_INDEX)
                                                        .withConsistentRead(false)
                                                        .withKeyConditionExpression("author = :v1")
                                                        .withExpressionAttributeValues(eav);

        List<Book> authorBook = mapper.query(Book.class, awayQuery);

        // need to create a new list because PaginatedList from query is immutable
        return new LinkedList<>(authorBook);
    }
    @Override
    public void saveOrUpdateBook(Book Book) {

        mapper.save(Book);
    }

    @Override
    public Optional<Book> findBookByBookId(String bookId) {

        Book book = mapper.load(Book.class, bookId);

        return Optional.ofNullable(book);
    }
    @Override
    public void deleteBook(String bookId) {

        Optional<Book> oBook = findBookByBookId(bookId);
        if (oBook.isPresent()) {
            mapper.delete(oBook.get());
        }
        else {
            log.error("Could not delete Book, no such bookId");
            throw new IllegalArgumentException("Delete failed for nonexistent Book");
        }
    }
}
