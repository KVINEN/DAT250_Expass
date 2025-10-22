import java.util.HashMap;
import java.util.Map;

public class ValkeyTest {

    private static io.valkey.JedisPool jedisPool;

    public static void main(String[] args) {
        io.valkey.JedisPoolConfig config = new io.valkey.JedisPoolConfig();

        config.setMaxTotal(32);
        config.setMaxIdle(32);
        config.setMinIdle(16);
        jedisPool = new io.valkey.JedisPool(config);
        try (io.valkey.Jedis jedis = jedisPool.getResource()) {
            System.out.println("Connection Successful: " + jedis.ping());

            // Basic Key-Value & Expiration
            System.out.println("\n--- Basic Key-Value & Expiration ---");
            jedis.set("user", "Alice");
            System.out.println("GET user: " + jedis.get("user"));
            jedis.expire("user", 5);
            System.out.println("TTL user (before expire): " + jedis.ttl("user"));
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("TTL user (after expire): " + jedis.ttl("user"));
            System.out.println("GET user (after expire): " + jedis.get("user"));

            //Use Case 1: Logged-in Users (Sets)
            System.out.println("\n--- Use Case 1: Keep track  of logged-in users ---");
            String loggedInUserKey = "logged_in_users";
            jedis.del(loggedInUserKey);

            jedis.sadd(loggedInUserKey, "Alice");
            jedis.sadd(loggedInUserKey, "Bob");
            System.out.println("Users logged in: " + jedis.smembers(loggedInUserKey));

            jedis.srem(loggedInUserKey, "Alice");
            System.out.println("Alice logged off. Users logged in: " +  jedis.smembers(loggedInUserKey));

            jedis.sadd(loggedInUserKey, "Eve");
            System.out.println("Eve logged in. Users logged in: " +  jedis.smembers(loggedInUserKey));
            System.out.println("Is Bob still logged in? " + jedis.sismember(loggedInUserKey, "Bob"));

            /*
            Use Case 2: Poll Data (Hashes)
            Do note I have not made the hashmap ordered
            so the way the data is inserted below
            is not how it will look when printed
            */

            System.out.println("\n--- Use Case 2: Poll data (Hashes) ---");
            String pollId = "poll:03ebcb7b-java";
            jedis.del(pollId);

            Map<String, String> pollData = new HashMap<>();
            pollData.put("title", "Pineapple on pizza?");
            pollData.put("option:0:description", "Yes yummy");
            pollData.put("option:0:voteCounter", "269");
            pollData.put("option:1:description", "Mamma mia, nooooo!");
            pollData.put("option:1:voteCounter", "268");
            pollData.put("option:2:description", "I do not care ...");
            pollData.put("option:2:voteCounter", "42");

            jedis.hmset(pollId, pollData);
            System.out.println("Poll data stored: " + jedis.hgetAll(pollId));

            jedis.hincrBy(pollId, "option:0:voteCounter", 1);
            System.out.println("Vote count 'Yes yummy' after increment: " + jedis.hget(pollId, "option:0:voteCounter"));


        } catch (Exception e) {
            e.getCause();
        }
        jedisPool.close();
    }
}
