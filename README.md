# Web-Data Indexer

A search engine for product reviews.</br>
The project was written as part of Web Information Retrieval course of The Hebrew University of Jerusalem.

## Technologies

The project is entirely written in Java and was devoloped and tested on Windows and Linux OS.

## Overview

The program supports reviews such as those available in “Fine Foods”, “Movies” etc., in
the [Amazon Dataset Collection](http://snap.stanford.edu/data/web-Amazon-links.html) from Stanford Large Network Dataset
Collection.</br>
The project contains three key components: Writer, Reader and Searcher

### Writer

Given A data set of reviews, the Writer creates a compressed index files for a future fast retrieval.</br>
The index creation is done according to an External-Sort based indexing technique:

* step1 - create token -> tokenID mapping
* step2 - create sorted sequences of pairs (tokenId,ReviewId)
* step3 - iterate the sequences by order and create the index

### Reader

Simple queries that help answer more sophisticated queries (such as the Searcher queries):

```java
/**
 * Returns the product identifier for the given review
 * Returns null if there is no review with the given identifier
 */
public String getProductId(int reviewId)

/**
 * Returns the score for a given review
 * Returns -1 if there is no review with the given identifier
 */
public int getReviewScore(int reviewId)

/**
 * Returns the numerator for the helpfulness of a given review
 * Returns -1 if there is no review with the given identifier
 */
public int getReviewHelpfulnessNumerator(int reviewId)

/**
 * Returns the denominator for the helpfulness of a given review
 * Returns -1 if there is no review with the given identifier
 */
public int getReviewHelpfulnessDenominator(int reviewId)

/**
 * Returns the number of tokens in a given review
 * Returns -1 if there is no review with the given identifier
 */
public int getReviewLength(int reviewId)

/**
 * Return the number of reviews containing a given token (i.e., word)
 * Returns 0 if there are no reviews containing this token
 */
public int getTokenFrequency(String token)

/**
 * Return the number of times that a given token (i.e., word) appears in the reviews indexed
 * Returns 0 if there are no reviews containing this token
 */
public int getTokenCollectionFrequency(String token)

/**
 * Return a series of integers of the form id-1, tokenFreq-1, id-2, tokenFreq-2, ... such that:
 * - id-n is the n-th review containing the given token
 * - tokenFreq-n is the number of times that the token appears in review id-n
 * Only return ids of reviews that include the token
 * Note that the integers should be sorted by id
 * Returns an empty Enumeration if there are no reviews containing this token
 */
public Enumeration<Integer> getReviewsWithToken(String token)

/**
 * Return the number of product reviews available in the system
 */
public int getNumberOfReviews()

/**
 * Return the number of tokens in the system
 * (Tokens should be counted as many times as they appear)
 */
public int getTokenSizeOfReviews()

/**
 * Return the ids of the reviews for a given product identifier
 * Note that the integers returned should be sorted by id
 * Returns an empty Enumeration if there are no reviews for this product
 */
public Enumeration<Integer> getProductReviews(String productId)
```

### Searcher

Allows the user to search products and reviews according to a given query.</br>
for example - search for the 100 most relevant movies according to the query "funny action movie"

## Performance

The project was designed to use less than 1GB of heap space, and was limited to that number during the tests.</br>
The testes where performed on data sets from
the [Amazon Dataset Collection](http://snap.stanford.edu/data/web-Amazon-links.html). </br>

data set                    | data set size| num of reviews| index creation time| index size
---                         | ---          | ---           | ---                | ---
Movies & TV (first million) | 1.04 GB      | 1,000,000     | 2.8 min            | 0.21 GB
Movies & TV                 | 8.56 GB      | 7,850,072     | 24 min             | 1.67 GB
Books                       | 14 GB        | 12,886,488    | 39 min             | 2.7 GB
All                         | 35 GB        | 34,686,770    | 130 min            | 6.7 GB

The Reader was also tested, and every method call took much less than 1 mSec.
