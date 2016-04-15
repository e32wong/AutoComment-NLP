import os
import re
import shutil

# gather a list of post questions

targetFolder = "/home/edmund/research/autocomment/posts/output/"
postFile = "/home/edmund/research/autocomment/posts/Posts.xml"

if not os.path.exists(targetFolder):
    os.makedirs(targetFolder)
else:
    shutil.rmtree(targetFolder)
    os.makedirs(targetFolder)

listPosts = []
with open(postFile, "r") as f:
    for line in f:
        m = re.match(".+PostTypeId=\"1\".+", line)
        if m:
            m = re.match(".+\\bId=\"([0-9]+)\".+", line)
            if m:
                listPosts.append(listPosts)
                id = m.group(1)

                f = open(targetFolder + id, 'w')
                f.write(line)
                f.close()

                print "Created " + id

with open(postFile, "r") as f:
    for line in f:
        m = re.match(".+PostTypeId=\"2\".+", line)
        if m:
            m = re.match(".+\\bParentId=\"([0-9]+)\".+", line)
            if m:
                parentID = m.group(1)

                f = open(targetFolder + parentID, 'a')
                f.write(line)
                print "Appended to " + parentID


