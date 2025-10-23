# DAT250 - Expass5

## Problems

 - Tried using redis. Moved away from it as i did not find an installation in the built-in installation tool for Omarchy.
 - Solution: Just use Valkey (keep it open-source)

 - Had another problem with CI/CD pipeline and Valkey. The test would always fail as it was trying to connect to valkey but never succeeded. 

[Jedis "Test"](https://github.com/KVINEN/DAT250_Expass/blob/main/src/main/java/ValkeyTest.java) \
[Cache](https://github.com/KVINEN/DAT250_Expass/blob/main/src/main/java/com/example/DAT250_Expass/Models/PollManager.java)
