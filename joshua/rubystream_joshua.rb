require 'rubygems'
require 'mysql2'
require 'oauth'
require 'json'
require 'tweetstream'

def testprint
  puts "hello world"
end

def connect_to_sql

	begin #java equivelance is try (and catch)
		con = Mysql2::Client.new(:host => "mysql5.dcs.warwick.ac.uk", :username => "csulbo", :password => "yT3H8rFxd7")

		#Try and find the database, if it doesn't exist, create it
		found = false
		databases = con.query("SHOW DATABASES")
		databases.each do |database|
			if database['Database']=="csulbodb"
				#if database exists, use it
				#Don't need to reapply the schema because want to keep all the data 
				#in there before starting
				con.query("USE csulbodb")
				found = true
				puts "Found the correct database"
        #Now check if the tables are there
        tables = con.query("show tables in csulbodb;")
        exists = tables.count
        if exists == 0
          puts "not found tables. Creating..."
          make_tables(con)
			 end  
      end
		end
		#if not found, create it 
		if found == false
			puts "Not found the database. Creating..."
			con.query("CREATE DATABASE csulbodb")
			make_tables(con)
		end
		
		authenticate()
    #checkRateLimit
    #getUser
    starting_point(con)
		
		rescue Exception => e #catch
			raise e

		ensure #will run regardless of failures
			con.close if con
		end
end

def make_tables(con)

  con.query("USE csulbodb")
	create_table_query = ""
	schema = File.new("schema.sql", "r")
	while (single_query = schema.gets)
   		create_table_query += "#{single_query}"
	end
	"#{create_table_query}".split(";").each { |query| con.query("#{query}")}
	schema.close
end

def starting_point(con)
  #starting point
   current_id = 27260086 #Starting with Justin Bieber
   #15439395 - stephenfry
   #72109014 - katie
   getMHRWnextnode(con, current_id)
end

#This probably needs to be given the sql connection, and the Twitter connection?
def getMHRWnextnode(con, current_id)
  # Don't forget to do burn in! 
    # And set a burnInThreshold
    
  
   
   current_num_all = 0
   while (current_id != nil)
     puts "starting current_id = " + current_id.to_s
       #Check if in database
       found = userToDatabaseHandler(con, current_id)
       #were they in the database?
       num_rows = found.count
       if num_rows == 0
         #If NOT found user, get the users details
         begin
            current = Twitter.user(current_id)
         rescue  Exception => e 
                 error_handler(con, e)
                retry
           # rescue
           #     puts "generic error. Retrying with new user"
           #     query = "select * from users;"
           #     query_result = con.query(query)
           #     new_current = getNeighbour(query_result)
           #     current_id = new_current["id"]
           #     retry
         end
         current_num_all = current["followers_count"] + current["friends_count"]
         #add user to users table in database
         query = "insert into users values(" + current_id.to_s + ", '" + current["screen_name"] + "', " + current["friends_count"].to_s + ", " + current["followers_count"].to_s + ", " + current["statuses_count"].to_s + ", 0, 0, 0, 0);"
         con.query(query)
         #get their initial 100 tweets, and send a kick counter of 0
         tweetsHandler(con, current_id, 1000, 0)
        else
         #How many tweets to grab???? Why not 100? Need to justify
         tweetsHandler(con, current_id, 500, 0)
         query = "select following_count, followed_by_count from users where user_ID = " + current_id.to_s + ";"
         count = con.query(query)
         count.each do |counter|
          current_num_all = counter["following_count"] + counter["followed_by_count"] 
        end
      end
         
         #grabs friends and followers into a databse. Updates cursors. 
         friendsAndFollowersHandler(con, current_id)
      
         #now need to find the next user to grab using MHRW
         #if cursor is null, get the first page 
         
         #MHRW
              # given the current node  
              # go to method to do with database and tweet collection
              # choose a random neighbour (need a method for that?)
              # get all neighbours of current node
              # get all neighbours of neighbour node
              # calculate ratio
              # make p a random number between 0 and 1
              # compare p and ratio
              # if p<=ratio
              #     current node = neighbour node
              # if p>ratio
              #   current node stays the same
         
         
         #Get all the users from the database to choose neighbour from
         query = "select * from following where user_id = " + current_id.to_s + " OR followed_by_id = " + current_id.to_s + ";"
         connections = con.query(query)
         
         new_current_id_flag = false
         kick_counter = 0
         puts "finding neighbour..."

         while(new_current_id_flag == false)
             
            
              neighbour_info = getRecursion(con, current_id, connections)
              neighbour_id = neighbour_info[:neighbour_id]
              neighbour_count = neighbour_info[:neighbour_count]

            begin
              ratio = current_num_all/neighbour_count
            rescue Exception => e
              puts "For neighbour: " + neighbour_id.to_s
            end
             
             p = rand()
             if p <= ratio || kick_counter > 2*current_num_all
               #current id update to neighbour_ID
               current_id = neighbour_id
               new_current_id_flag = true
             else
               #current id stays the same
             end
             kick_counter = kick_counter+1
         end  #ends while current user flag not updated
         
  end #ends while current_id != nil
  puts "DONE"
end

def getRecursion(con, current_id, connections)

  to_return = {:neighbour_id => 0, :neighbour_count => 0}
  #get a random user (from database, not twitter) back from query of friends and followers of current_id
  chosen = getNeighbour(connections)
            
  if chosen["user_ID"]==current_id #neighbour is followed_by_id
    to_return[:neighbour_id] = chosen["followed_by_id"] 
  else #neighbour is user_ID
    to_return[:neighbour_id] = chosen["user_ID"]
  end
 
  #checks if user is in database
  found = userToDatabaseHandler(con, to_return[:neighbour_id])
  num_rows = found.count
  #if not, get them from twitter
  if num_rows == 0
      begin
        neighbour = Twitter.user(to_return[:neighbour_id])
        to_return[:neighbour_count] = neighbour["followers_count"] + neighbour["friends_count"] 
      rescue  Exception => e 
          error = error_handler(con, e)
          if error == "NotFound"
            to_return = getRecursion(con, current_id, connections)
          else
            retry
          end
      end#ends begin, resuce
      
  else#else grab the info from database
      query = "select following_count, followed_by_count from users where user_ID = " + to_return[:neighbour_id].to_s + ";"
      neighbour_db = con.query(query)
      neighbour_db.each do |n|
        to_return[:neighbour_count] = n["following_count"] + n["followed_by_count"] 
        puts "DB, adding " + n["following_count"].to_s + " " + n["followed_by_count"].to_s
      end#ends do loop
 end#ends if user in database (num_rows == 0)

 return to_return

end

def getNeighbour(query_result)
    #what if they don't have followers
     random = rand(query_result.count)
     chosen = nil
     counter = 0
     query_result.each do |connection|
       if counter==random
          chosen = connection
          break
       else
         counter=counter+1
       end 
     end
     
     return chosen
end

def getUser
 #This method is used for testing authentication. Sometimes for finding an id of a friend. Never called
  
  Twitter.configure do |config2|
          config2.consumer_key       = 'Tr7OSqwXVUAKgmgWe17sUQ'
          config2.consumer_secret    = 'tuY1yAYq2Ew7go12MNika6BC3uyrpC3LCpoVBZk'
          config2.oauth_token        = '1653234812-kuXpFwQz5U3i4tlbNWonEeL49HqHcFyh2Au4vCk'
          config2.oauth_token_secret = 'OFcZMVfu2d2EOcvnifjUwPvlWWRPYsDr5OA4eTM58'
     end
     
  #katie = Twitter.user("katie_bell_93")
  #p katie.inspect
  stephenfry = Twitter.user(51722563)
  p stephenfry.inspect
# p stephenfry["id"].inspect #15439395
  
  #current = Twitter.get('https://api.twitter.com/1.1/users/show.json?user_id=72109014')
 #p current.inspect
  #p katie["id"].inspect
   #p stephenfry["id"].inspect
end

def userToDatabaseHandler(con, user_id)
 
  #query if user is in database
  #if not
  # add to database
  # else continue to tweet collection (maybe need to update fact visiting again
  # Return true if user was already in database, and false otherwise (but they will have been added after this)
  #
  begin
      query = "select * from users where user_id = " + user_id.to_s + ";" 
      found = con.query(query)
  rescue  Exception => e 
        error_handler(con, e)
        make_tables(con)
        retry
  end
  return found
end

def tweetsHandler(con, current_id, count, kick_counter)
 

  #going to have to specify here the since_id and max_id to make sure not grabbing same tweets
  max_id_db = 0
  since_id_db = 0
  since_id_to_store = 0
  max_id_to_store = 0
  tweetcount = 0
  timeline = nil


  #need to get tweets with max_id and since_id from db
  ids = con.query("select max_id, since_id from users where user_ID =" + current_id.to_s + ";")
    ids.each do |row|
      max_id_db = row["max_id"]
      since_id_db = row["since_id"]
    end

    #need to check this person tweeted before, if not, check if they have now, else, use since and max_ids
    if since_id_db == 0 && max_id_db == 0
      begin
       timeline = Twitter.user_timeline(current_id, :count => count) 
      rescue Exception => e
        error_handler(con, e)
      end

      tweetcount = timeline.count

    else #use since and max ids
      begin
        timeline1 = Twitter.user_timeline(current_id, :count => count, :since_id => since_id_db)
        tweetcount = timeline1.count
        remainder = count - tweetcount
        timeline2 = Twitter.user_timeline(current_id, :count => remainder, :max_id => max_id_db)
          
        if tweetcount == 0 #if no new tweets, keep since id as was in db
              since_id_to_store = since_id_db
        else #else set it a flagged value so it gets changed
              since_id_to_store = 0
        end

        timeline = timeline1.concat(timeline2)
        tweetcount = timeline.count

      rescue Exception => e
        error_handler(con, e)
        retry
      end#ends begins/rescue
         
    end#ends if/else for if since and max ids were 0
              
    #set another variable to tweetcount, so that we have something to check against later
    #if a tweet isn't english we need to skip over it
    #so later need to make up the number of tweets stored
    stored_counter_check = tweetcount
     
    #iterate over tweets grabbed for storing
    timeline.each do |tweet|



             #might need a better way to do this, but need ID of first iteration only
           if since_id_to_store == 0
             since_id_to_store = tweet["id"]
           end
          
           #last tweet processed will be correct id for max_id.
           max_id_to_store = tweet["id"]


            #Don't want to do sentiment analysis on a tweet if it isn't english
            #Skip over an iteration if it isn't english
            #Do this after storing min and max ids so that next timeline grab will think
            #we've grabbed this and if we run out of tweets can continue as normal

            #if the tweet is the last one we stored previously (happens based on api) then skip over because we have it
            if tweet["id"] == max_id_db
              next
            end

            #if it isn't english, skip over it and reduce how many we've stored so we can try and grab more for this user after
            if tweet["lang"] != "en" 
              tweetcount = tweetcount-1
              next
            end

           #based on value of original, know whether to store in original or retweets
           #need to search if the original has already been put in the database
           
           #if this tweet is a retweet
           if tweet["retweeted_status"] 
                  
                #search to see if original has been stored, will break foreign key constraint if not
                found = con.query("select tweet_ID from original_tweets where tweet_ID = " + tweet["retweeted_status"]["id"].to_s + ";")
                num_rows = found.count
                
                #if there were no row, it wasn't found, need to store original tweet 
                if num_rows == 0 

                      tweetText = tweetCleaner(tweet["retweeted_status"]["text"])
                      #original_tweets: tweet_id, user_id, favourite_count, retweet_count
                      begin
                        query = "insert into original_tweets values(" + tweet["retweeted_status"]["id"].to_s + ", " + tweet["retweeted_status"]["user"]["id"].to_s + ", \"" + tweetText.to_s + "\", " + tweet["retweeted_status"]["favourite_count"].to_s + ", " + tweet["retweeted_status"]["retweet_count"].to_s + ");"
                        con.query(query)
                      rescue
                        puts "Tweet ID with problem: " + tweet["retweeted_status"]["id"].to_s
                        error_handler(con, e)
                        puts "rescuing sql insert to original tweets"
                      end
                else #found original in database, might need to update values for retweet and favourite counts though
                      query = "update original_tweets set favourite_count = " + tweet["retweeted_status"]["favourite_count"].to_s + " where tweet_ID = " + tweet["retweeted_status"]["id"].to_s + ";"
                      con.query(query)
                      query =  "update original_tweets set retweet_count = " + tweet["retweeted_status"]["retweet_count"].to_s + " where tweet_ID = " + tweet["retweeted_status"]["id"].to_s + ";"   
                      con.query(query)         
                end
               
               #retweets: tweet_ID, retweeted_by_id, original_tweet_id, originally_tweeted_by_id 
                begin
                   query = "insert into retweets values(" + tweet["id"].to_s + ", " + current_id.to_s + ", " + tweet["retweeted_status"]["id"].to_s + ", " + tweet["retweeted_status"]["user"]["id"].to_s + ");"
                   con.query(query)
                rescue Exception => e 
                   error_handler(con, e)

                end
                 
           else #if original tweet
               #need to add to original_tweets
               begin
                   tweetText = tweetCleaner(tweet["text"])
                   query = "insert into original_tweets values(" + tweet["id"].to_s + ", " + current_id.to_s + ", \"" + tweetText.to_s + "\", " + tweet["favourite_count"].to_s + ", " + tweet["retweet_count"].to_s + ");"
                   con.query(query)
                rescue Exception => e 
                   error_handler(con, e)
               end
           end #ends if original   
     end #ends tweet do loop
  
  con.query("update users set since_id = " + since_id_to_store.to_s + " where user_ID = " + current_id.to_s + ";")
  con.query("update users set max_id = " + max_id_to_store.to_s + " where user_ID = " + current_id.to_s + ";")

  #Here check if we got the correct number of tweets
  #May not have done if had to filter some none english ones out
  if tweetcount != stored_counter_check 
      #if the max id that keeps getting stored isn't english, suck in infinite loop
      #will keep grabbing it and keep checking, keep reducing tweet count
      count = stored_counter_check - tweetcount
      puts "tweets saved: " + tweetcount.to_s + " out of: " + stored_counter_check.to_s
      if kick_counter < 5
        kick_counter = kick_counter + 1
        tweetsHandler(con, current_id, count, kick_counter)
      else
        puts "Due to kick counter, we are skipping over these tweets"
      end
  end
end

def tweetCleaner(tweetText)
  
  tweetText = tweetText.gsub("\"", "\\\"")
  tweetText = tweetText.gsub("'", "\\\\'")
  tweetText = tweetText.gsub("%", "\%")
  tweetText = tweetText.gsub("_", "\_")
  tweetText = tweetText.gsub("%", "\%")

  return tweetText
end

def friendsAndFollowersHandler(con, current_id)
  
  
    next_cursor_followers = 0
    next_cursor_friends = 0
  
   cursors = con.query("select next_cursor_friends, next_cursor_followers from users where user_ID = " + current_id.to_s + ";")   
   cursors.each do |row|
       next_cursor_followers = row["next_cursor_followers"]
       next_cursor_friends = row["next_cursor_friends"]
       end
     
   begin  
       if next_cursor_friends == 0
         friends = Twitter.friend_ids(current_id, :count => 100)
       else
         friends = Twitter.friend_ids(current_id, :cursor => next_cursor_friends, :count => 100)
       end
   rescue Exception => e 
       error_handler(con, e)
        retry
   end
   
   if friends != nil
      friends.ids.each do |id| 
         begin
             query = "insert  into following values(" + id.to_s + ", " + current_id.to_s + ");"
             con.query(query)
         rescue
             #puts "rescuing adding friends problem"
         end
       end
       query = "update users set next_cursor_friends = " + friends.next_cursor.to_s + " where user_ID = " + current_id.to_s + ";"
       con.query(query)
   end
  
  #puts "followers:"
   begin 
       if next_cursor_followers == 0
         followers = Twitter.follower_ids(current_id, :count => 100)
       else
         followers = Twitter.follower_ids(current_id, :cursor => next_cursor_followers, :count => 100)
       end
   rescue Exception => e 
       error_handler(con, e)
       retry
   end
   
   if followers != nil
     followers.ids.each do |id| 
           begin
               query = "insert  into following values(" + current_id.to_s + ", " + id.to_s + ");"
               con.query(query)
           rescue
            #adding follower to database problem
           end
       end
       query = "update users set next_cursor_followers = " + followers.next_cursor.to_s + " where user_ID = " + current_id.to_s + ";"
       con.query(query)
   end
end

def error_handler(con, e)
    
    #need to edit Twitter::Error::Forbidden
    #
    #
    #
    #
    puts "***In error_handler"
    error = e.inspect.to_s

    if error.include? "Interrupt"
      raise e
    end

    #too many requests
    if error.include? "Twitter::Error::TooManyRequests"
      time = Time.now.getutc
      puts "error_handler: too many requests. Sleeping at: " + time.to_s
      sleep(900)
    #tried to access a non-existent resource
    elsif error.include? "Twitter::Error::NotFound"
      #need a new neighbour
      #execute a method to do it?
      puts "error NotFound, starting recursion :s"
      return "NotFound"
    #error is forbidden (not completely sure what this means)
    elsif error.include? "Twitter::Error::Forbidden"
      return "NotFound"
    #random unauthorized error
    elsif error.include? "Twitter::Error::Unauthorized"
      puts "error_handler: unauthorized"
      #need to get a new user
      results = con.query("select user_ID from users")
      new_id = getNeighbour(results)
      getMHRWnextnode(con, new_id["user_ID"])
    elsif error.include? "Twitter::Error::ServiceUnavailable"
      #sleep for 2 hours
      sleep(7200)
    #sql error
    elsif error.include? "Mysql2::Error"
      puts "error_handler: mysql"
      if error.include? "Duplicate"
        #nothing, hopefully continue
      else
        p e.inspect
        raise e
      end
    #any other error, raise
    else
      p e.inspect
      raise e
    end

  return "nothing to return from error handler"

end

def checkRateLimit
  #
  #
     remaining = Twitter.get('/1.1/application/rate_limit_status.json')[:body]
     jj remaining
end

def authenticate()
  #
  #
  Twitter.configure do |config3|
           config3.consumer_key       = 'Tr7OSqwXVUAKgmgWe17sUQ'
           config3.consumer_secret    = 'tuY1yAYq2Ew7go12MNika6BC3uyrpC3LCpoVBZk'
           config3.oauth_token        = '1653234812-kuXpFwQz5U3i4tlbNWonEeL49HqHcFyh2Au4vCk'
           config3.oauth_token_secret = 'OFcZMVfu2d2EOcvnifjUwPvlWWRPYsDr5OA4eTM58'
      end
  
 

  
end

connect_to_sql