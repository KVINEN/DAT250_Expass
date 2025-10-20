# Dat250 - Expass4

### Problem 1:
Hibernate Query Language failing. \
PollsTest.java failed due to it using specific names like (options, createdBy, votesOn) that did not match the actual names I had in my Java Entity classes (voteOption, user, votingOption). \
##### Solution: Renaming
 - Poll.java, user -> createdBy
 - Poll.java, voteOption -> options
 - Vote.java, votingOption -> votesOn
 - Updated corresponding getters, setters, and contractor parameters

### Problem 2:
Incorrect relationship mapping. \
Caused by a mapping in user.java, the OneToMany for createdPolls field used mappedBy = "user". \
This was a problem as I had renamed it in poll.java to createdBy.

##### Solution: Rename
 - user.java, @OneToMany(mappedBy = "user",......) -> @OneToMany(mappedBy = "createdBy",.....)

### Problem 3:
Some of my old tests from expass2 started failing after I changed my entities. \
This was caused by the fact that I renamed and changed some of the names of get/set votingOption -> get/set votesOn in Vote.java. \

##### Solution:
 - I kept the old getters and setters but introduced the annotations @Transient and @Deprecated 

### OnGoing problem:

Never got the H2 server to work still trying to figure it out