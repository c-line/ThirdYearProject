DROP TABLE IF EXISTS users;
CREATE TABLE users  (
  user_ID           integer(10),
  username          varchar(50),
  following_count   integer(7),
  followed_by_count integer(7),
  tweet_count       integer(10),
  primary key (user_ID)
);

DROP TABLE IF EXISTS tweets;
CREATE TABLE tweets (
  tweet_ID          integer(20) not null,
  tweet             varchar(160) not null,
  retweeted         boolean not null, #if this status has been retweeted
  retweet_count     integer(10), #if so, how many times
  favourite_count   integer(10), #how many times tweet was favourited. 
  primary key (tweet_ID)
);

DROP TABLE IF EXISTS tweets_from_user;
CREATE TABLE tweets_from_user (
  tweet_ID  integer(20) not null,
  user_ID   integer(10) not null,
  primary key (tweet_ID, user_ID),
  foreign key (user_ID) references users(user_ID),
  foreign key (tweet_ID) references tweets(tweet_ID)
);

DROP TABLE IF EXISTS retweets;
CREATE TABLE retweets (
  tweet_ID                  integer(20) not null,
  retweeted_by_id           integer(10) not null,
  originally_tweeted_by_id  integer(10) not null,
  primary key (tweet_ID, retweeted_by_id, originally_tweeted_by_id),
  foreign key (tweet_ID) references tweets(tweet_ID),
  foreign key (retweeted_by_id) references users(user_ID),
  foreign key (originally_tweeted_by_id) references users(user_ID)
);

#first id is being followed by second id
DROP TABLE IF EXISTS following;
CREATE TABLE following (
  user_ID         integer(10) not null, 
  followed_by_id  integer(10) not null, 
  primary key (user_ID, followed_by_id)
);

DROP TABLE IF EXISTS users_hashtags;
CREATE TABLE users_hashtags (
  user_ID   integer(10),
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
  user_ID     integer(10) not null,
  tweet_ID    integer(20) not null,
  primary key (user_ID, tweet_ID),
  foreign key (user_ID) references users(user_ID),
  foreign key (tweet_ID) references tweets(tweet_ID)
);