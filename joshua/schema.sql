DROP TABLE IF EXISTS users;
CREATE TABLE users  (
  user_ID                 bigint(30),
  username              varchar(50),
  following_count       integer(7),
  followed_by_count     integer(7),
  tweet_count           integer(10),
  next_cursor_followers   bigint(25), 
  next_cursor_friends   bigint(25), 
  since_id          bigint(30),#for tweets
  max_id          bigint(30),#for tweets
  primary key (user_ID)
);

DROP TABLE IF EXISTS original_tweets;
CREATE TABLE original_tweets (
  tweet_ID  bigint(30) not null, #cannot be a foreign key because it may be a tweet from a user wehave not visited, so will not be in tweet table
  user_ID   bigint(30) not null, #cannot be a foreign key because while we want this info, do not want to grab this user if it was not from MHRW. Would be sidetracking
  text    varchar(200),
  favourite_count   integer(10), #how many times tweet was favourited
  retweet_count     integer(10), #how many times
  primary key (tweet_ID)
);

DROP TABLE IF EXISTS retweets;
CREATE TABLE retweets (
  #retweets
  tweet_ID                  bigint(30) not null,
  retweeted_by_id           bigint(30) not null,
  original_tweet_ID     bigint(30) not null,
  originally_tweeted_by_id  bigint(30) not null, #This person may not be in users table, hence not foreign key
  primary key (tweet_ID, retweeted_by_id),
  foreign key (original_tweet_ID) references original_tweets(tweet_ID),
  foreign key (retweeted_by_id) references users(user_ID)
);

#first id is being followed by second id
DROP TABLE IF EXISTS following;
CREATE TABLE following (
  user_ID         bigint(30) not null, 
  followed_by_id  bigint(30) not null, 
  primary key (user_ID, followed_by_id)
);


DROP TABLE IF EXISTS users_hashtags;
CREATE TABLE users_hashtags (
  user_ID   bigint(30),
  hashtag   varchar(150),
  count     integer(5),
  primary key (user_ID, hashtag),
  foreign key (user_ID) references users(user_ID)
);

DROP TABLE IF EXISTS hashtags;
CREATE TABLE hashtags (
  hashtag varchar(150) not null,
  count   integer(10) not null,
  primary key (hashtag)
);

#indicates a user has favourited that tweet. (Maybe include id of who wrote the original tweet?)
DROP TABLE IF EXISTS favourite_tweets;
CREATE TABLE favourite_tweets (
  user_ID     bigint(30) not null,
  tweet_ID    bigint(30) not null,
  primary key (user_ID, tweet_ID),
  foreign key (user_ID) references users(user_ID),
  foreign key (tweet_ID) references original_tweets(tweet_ID)
);