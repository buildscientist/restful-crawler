#!/bin/bash 
URL='http://localhost'

#Comment out lines per endpoint as neccessary

#GET / - returns an XML document with a description of the API
curl -vvv $URL/

#DELETE /purge/all
curl -vvv -X delete $URL/purge/all

#GET /crawler
curl -vvv $URL/crawler?url=http:www.depaul.edu&depth=3&max=1000

#GET /crawler/status
curl -vvv $URL/crawler/status

#GET /list/documents
curl -vvv $URL/clist/documents

#GET /find
curl -vvv $URL/find?url=http://www.depaul.edu

#DELETE /purge
curl -vvv -X delete $URL/purge?url=http://www.depaul.edu

#GET /query/xpath
curl -vvv $URL/query/xpath?expression=//a/@href[contains(.,'http:/www.depaul.edu/')]

#GET /query/stax
curl -vvv $URL/query/xpath?element=foo&text=bar

