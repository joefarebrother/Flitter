'''
Created on 30 Mar 2018

@author: pings
'''

'''
pretty-printing the json data file
'''

import json

filepath = './data.json'

if __name__ == '__main__':
    # load the data 
    with open(filepath) as f:
        data = json.load(f)
    
    # save the data with indenting
    with open('./data_formatted.json', 'w+') as f:
        json.dump(data, f, indent=4)