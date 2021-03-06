package com.example.auction.user.impl;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.example.auction.user.impl.PUserCommand.CreatePUser;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.*;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UserEntityTest {

    private static ActorSystem system;
    private PersistentEntityTestDriver<PUserCommand, PUserEvent, Optional<PUser>> driver;

    @BeforeClass
    public static void startActorSystem() {
        system = ActorSystem.create("UserEntityTest");
    }

    @AfterClass
    public static void shutdownActorSystem() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    private final UUID id = UUID.randomUUID();
    private final String name = "admin";
    private final String email = "admin@gmail.com";


    private final PUser user = new PUser(id, name, email);


    @Before
    public void createTestDriver() {
        driver = new PersistentEntityTestDriver<>(system, new PUserEntity(), id.toString());
    }

    @After
    public void noIssues() {
        if (!driver.getAllIssues().isEmpty()) {
            driver.getAllIssues().forEach(System.out::println);
            fail("There were issues " + driver.getAllIssues().get(0));
        }
    }

    @Test
    public void testCreateUser() {
        Outcome<PUserEvent, Optional<PUser>> outcome = driver.run(
                new CreatePUser(user));
        assertEquals(user, outcome.getReplies().get(0));
        assertEquals(Collections.emptyList(), driver.getAllIssues());
    }

    @Test
    public void testRejectDuplicateCreate() {
        driver.run(new CreatePUser(user));
        Outcome<PUserEvent, Optional<PUser>> outcome = driver.run(
                new CreatePUser(user));
        assertEquals(PUserEntity.InvalidCommandException.class, outcome.getReplies().get(0).getClass());
        assertEquals(Collections.emptyList(), outcome.events());
        assertEquals(Collections.emptyList(), driver.getAllIssues());
    }

}
