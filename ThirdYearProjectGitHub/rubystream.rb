require 'rubygems'
require 'tweetstream'
require 'mysql2' #uses a "gem" same as import java.util for example
require 'oauth'
require 'json'

def testprint
  puts ("hello world")
end
def connect_to_sql

	begin #java equivelance is try (and catch)
		con = Mysql2::Client.new(host: 'localhost', username: 'root', password: '')


		#Try and find the database, if it doesn't exist, create it
		found = false
		databases = con.query("SHOW DATABASES")
		databases.each do |database|
			if database['Database']=="thirdyearproject"
				#if database exists, use it
				#Don't need to reapply the schema because want to keep all the data 
				#in there before starting
				con.query("USE thirdyearproject")
				found = true
				puts "Found the correct database"
			end
		end
		#if not found, create it 
		if found == false
			puts "Not found the database, Creating..."
			con.query("CREATE DATABASE thirdyearproject")
			con.query("USE thirdyearproject")
			make_tables(con)
		end
		
		rescue Exception => e #catch
			raise e

		ensure #will run regardless of failures
			con.close if con
		end
	end

def make_tables(con)

	create_table_query = ""
	schema = File.new("schema.sql", "r")
	while (single_query = schema.gets)
   		create_table_query += "#{single_query}"
	end
	"#{create_table_query}".split(";").each { |query| con.query("#{query}")}
	schema.close
end

def twitterstreamfilter

	TweetStream.configure do |config|
	  config.consumer_key       = 'Tr7OSqwXVUAKgmgWe17sUQ'
	  config.consumer_secret    = 'tuY1yAYq2Ew7go12MNika6BC3uyrpC3LCpoVBZk'
	  config.oauth_token        = '1653234812-kuXpFwQz5U3i4tlbNWonEeL49HqHcFyh2Au4vCk'
	  config.oauth_token_secret = 'OFcZMVfu2d2EOcvnifjUwPvlWWRPYsDr5OA4eTM58'
	  config.auth_method        = :oauth
	end
	
	TweetStream::Client.new.sample do |tweet|
		if tweet["lang"]=="en"
			query = "INSERT INTO tweets VALUES(#{tweet['id']}, #{tweet['text']}"
			#con.query("INSERT INTO tweets VALUES (tweet['user']tweet['id'], tweet['text'], tweet['id'], tweet['reweet'], tweet['retweeted_status']['retweet_count']")
			if tweet["retweeted_status"]
				query = query + ", true, #{tweet['retweeted_status']['retweet_count']}, #{tweet['retweeted_status']['favourite_count']});"
				#p tweet.inspect
				#p tweet["retweet_count"]
				#p tweet["text"]
				#p tweet["user"]["id"]
				#p tweet["user"]["screen_name"]
				#puts "this has a retweeted status"
				#p tweet["retweeted_status"]["retweet_count"]
			else 
				query = query + ", false, #{tweet['retweet_count']}, #{tweet['favourite_count']});"
			
			query = query + "INSERT INTO "
			puts query
			#con.query(query)

			


			#If the tweet object is a retweet, then the it contains a tweet object in it with the original retweet
			end
		end #if tweet lang=en end
	end
end


connect_to_sql

#twitterstreamfilter


