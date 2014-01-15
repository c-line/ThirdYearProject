require 'rubygems'
require 'mysql2'
require 'oauth'
require 'json'
require 'tweetstream'


def getTweet
 #This method is used for testing authentication. Sometimes for finding an id of a friend. Never called
  
  Twitter.configure do |config2|
          config2.consumer_key       = 'Tr7OSqwXVUAKgmgWe17sUQ'
          config2.consumer_secret    = 'tuY1yAYq2Ew7go12MNika6BC3uyrpC3LCpoVBZk'
          config2.oauth_token        = '1653234812-kuXpFwQz5U3i4tlbNWonEeL49HqHcFyh2Au4vCk'
          config2.oauth_token_secret = 'OFcZMVfu2d2EOcvnifjUwPvlWWRPYsDr5OA4eTM58'
     end
     
 

  tweet = Twitter.status(183620852011642880)
  #p tweet.inspect
  p tweet["text"]
# p stephenfry["id"].inspect #15439395
  
  #current = Twitter.get('https://api.twitter.com/1.1/users/show.json?user_id=72109014')
 #p current.inspect
  #p katie["id"].inspect
   #p stephenfry["id"].inspect
end

getTweet