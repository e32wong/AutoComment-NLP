
import re

listPosts = []
with open("/home/edmund/research/autocomment/posts/Posts.xml", "r") as f:
    for line in f:
        m = re.match(".+PostTypeId=\"1\".+", line)
        if m:
            m = re.match(".+Id=\"([0-9]+)\".+", line)
            if m:
                print m.group(1)
        else:
            print "2"






