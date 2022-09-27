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


package com.amazonaws.serverless.function;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import com.amazonaws.serverless.dao.DynamoDBBookDao;
import com.amazonaws.serverless.pojo.Title;
import com.amazonaws.serverless.util.Consts;
import org.apache.log4j.Logger;

import com.amazonaws.serverless.domain.Book;
import com.amazonaws.serverless.pojo.Author;


public class BookFunctions {

    private static final Logger log = Logger.getLogger(BookFunctions.class);

    private static final DynamoDBBookDao BookDao = DynamoDBBookDao.instance();


    public List<Book> getAllBooksHandler() {

        log.info("GetAllBooks invoked to scan table for ALL Books");
        List<Book> Books = BookDao.findAllBooks();
        log.info("Found " + Books.size() + " total Books.");
        return Books;
    }

    public List<Book> getBooksForTitle(Title title) throws UnsupportedEncodingException {

        if (null == title || title.getTitle().isEmpty() || title.getTitle().equals(Consts.UNDEFINED)) {
            log.error("GetBooksForTitle received null or empty title");
            throw new IllegalArgumentException("Title cannot be null or empty");
        }

        String titleName = URLDecoder.decode(title.getTitle(), "UTF-8");
        log.info("GetBooksForTitle invoked for title = " + titleName);
        List<Book> Books = BookDao.findBooksByTitle(titleName);
        log.info("Found " + Books.size() + " Books for title = " + titleName);

        return Books;
    }

    public List<Book> getBooksForAuthor(Author author) throws UnsupportedEncodingException {

        if (null == author || author.getAuthor().isEmpty() || author.getAuthor().equals(Consts.UNDEFINED)) {
            log.error("GetBooksForAuthor received null or empty author name");
            throw new IllegalArgumentException("Author name cannot be null or empty");
        }

        String authorName = URLDecoder.decode(author.getAuthor(), "UTF-8");
        log.info("GetBooksForAuthor invoked for author with name = " + authorName);
        List<Book> Books = BookDao.findBooksByAuthor(authorName);
        log.info("Found " + Books.size() + " Books for author = " + authorName);

        return Books;
    }

    public void saveOrUpdateBook(Book book) {

        if (null == book) {
            log.error("SaveBook received null input");
            throw new IllegalArgumentException("Cannot save null object");
        }

        log.info("Saving or updating Book for title = " + book.getTitle());
        BookDao.saveOrUpdateBook(book);
        log.info("Successfully saved/updated Book");
    }

    public String deleteBook(String bookId) {

        log.info("Deleting Book for bookId = " + bookId);
        BookDao.deleteBook(bookId);
        log.info("Successfully deleted Book");
        return "Success";
    }

}
