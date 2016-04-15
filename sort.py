#!/usr/bin/python

import HTMLParser
import os
import re
import shutil

# edit these variables
tag = "java"
targetFolder = "/home/edmund/research/autocomment/posts/java/"
postFile = "/home/edmund/research/autocomment/posts/Posts.xml"

if not os.path.exists(targetFolder):
    os.makedirs(targetFolder)
else:
    shutil.rmtree(targetFolder)
    os.makedirs(targetFolder)

html_parser = HTMLParser.HTMLParser()


listPosts = []
with open(postFile, "r") as f:
    for line in f:
        m = re.match(".+PostTypeId=\"1\".+", line)
        if m:
            # get the id number
            m = re.match(".+\\bId=\"([0-9]+)\".+", line)
            if m:
                PostId = m.group(1)

                m = re.match(".+\\bTags=\"(.+?)\"\s.+", line)
                if m:
                    tagListOrg = m.group(1)
                    tagListEsc = html_parser.unescape(tagListOrg)

                    if "<" + tag + ">" in tagListEsc:

                        listPosts.append(listPosts)

                        f = open(targetFolder + PostId, 'w')
                        f.write(line)
                        f.close()

                        print "Created " + PostId

with open(postFile, "r") as f:
    for line in f:
        m = re.match(".+PostTypeId=\"2\".+", line)
        if m:
            m = re.match(".+\\bParentId=\"([0-9]+)\".+", line)
            if m:
                parentID = m.group(1)
                parentFile = targetFolder + parentID
                # make sure the parent file exist
                if os.path.isfile(parentFile):
                    f = open(parentFile, 'a')
                    f.write(line)
                    print "Appended to " + parentID
                    f.close()


