import tweepy
from tweepy import OAuthHandler
from tweepy import Stream
from tweepy.streaming import StreamListener
import json
 
# Connect with twitter api 
consumer_key = 'nbeRcujbrGGTFuULv8V0LOJuA'
consumer_secret = 'rl9GrFcgl16MJ3ovIk4OnCEJpoGx0lczyBKALo6VIpAUe0kFlT'
access_token = '2352981716-Q0gYaNXjKFlTaFV3FF46nPOkjzfn0QgFeMkQtgr'
access_secret = '4dSRhQQ4OckbIQ5629YTbtIHbLtUVb5knQuSQRu6X0wIq'
 
auth = OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token, access_secret)
 
api = tweepy.API(auth)
		
			
class MyListener(StreamListener):
	
		def on_data(self, data):
			try:
				with open('data.json', 'a') as f:
					f.write(data)
				return True
			except BaseException as e:
				print("Error on_data: %s" % str(e))
			return True
 
		def on_error(self, status):
			print(status)
			return True
 
mylistener = MyListener()
twitter_stream = tweepy.Stream(auth, mylistener)
twitter_stream.filter(track=['british summer', 'brexit', 'marvel'])
twitter_stream.filter(locations!= null)
twitter_stream.filter(retweet_count > 10)
