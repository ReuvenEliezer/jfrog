# Improvement to production
1. Readability - extract impl to a smaller function, const etc..
2. Make the main process com.reuven.jfrog.services.GitHubItemsRetrieverServiceImpl.RetrieverData func to a ThreadPool and run together (by urls.size()) 
3. Consider to transfer the code to Reactive (non-blocking) in order to reduce the CPU & Memory when we waiting to GitHub API response (it's reduce your computing on cloud for the same traffic) (still needs to block between pages)
4. In case of user sent a request that have a large response - consider to make the process @Async and retrive to the user (after a basic validation) 202 HTTPCode (acceptable) 
   with a link included a new URL (bucket, key) to a cloud storage like S3 (key include userId, timestamp and requestId (uuid.random) (nake it secured)
   so, store the result in S3 as a batch every a limitation of memory and release the stack.
    needs to lock the file - because aggrigation value and make it Atomic. or do it with a batch process like Spark - aggrigate after all consumer will be done

   to make it robustic (for a 403 Forbidden or networks failures) (don't loss any result)- we can to change the handler from our micro-service to Kafka with knowledge strategy. add @Spring Retry strategy for each Exception from GitHub APi. the API will be @Async and produce the new request for each URL (with a given bucket key in s3) and the kafka consumer handle each URL by self.
   the consumer save the result in s3 specific key that given in the message.

   in case we want to inform the user only when the process is finished -  
   needs to handle it in a separate logic as bellow:
   - the MS API save a ReqId with status and stage. + save in DB the ReqId with num_url_required=url size & num_url_processed=0)
   - produce a new message to kafka consumer for each url with a specific destination in s3
   - consumer do the process and aggrigate result - (needs lock the agg)
   - after consumer is done to handle a specific URL -> he send a new status message to a diff kafka topic. 
   - the status_url topic increase the num_url_processed and compare it to num_url_required. if it equale -> send a notification with the URL to the user
     can to to it also by Aws-Lambda triggered by s3-event
5. Tests & IT
6. Logs

