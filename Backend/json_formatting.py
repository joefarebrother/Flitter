'''
Created on 30 Mar 2018

@author: pings
'''

'''
pretty-printing the json data file
'''

import json

filepath = './data.json'



data = []
seen_ids = []
def register_tweet(tw):
	global data, seen_ids
	if (tw["id_str"] not in seen_ids):
		data.append(tw)
    seen_ids.append(tw["id_str"])

# load the data 
with open(filepath) as f:
  for line in f:
    tw = json.loads(line)
    register_tweet(tw)
    if ("retweeted_status" in tw and tw["retweeted_status"]):
    	register_tweet(tw["retweeted_status"])
    if ("quoted_status" in tw and tw["quoted_status"]):
    	register_tweet(tw["quoted_status"])

# save the data with indenting
with open('./data_formatted.json', 'w+') as f:
  json.dump(data, f, indent=4)