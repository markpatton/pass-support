package org.eclipse.pass.support.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.eclipse.pass.support.client.JsonApiPassClient;
import org.eclipse.pass.support.client.PassClient;
import org.eclipse.pass.support.client.adapter.ZonedDateTimeAdapter;
import org.eclipse.pass.support.client.model.Funder;
import org.eclipse.pass.support.client.model.Grant;
import org.eclipse.pass.support.client.model.Publication;
import org.eclipse.pass.support.client.model.User;
import org.eclipse.pass.support.client.model.UserRole;
import org.eclipse.pass.support.client.model.support.TestValues;
import org.junit.jupiter.api.Test;

public class JsonApiPassClientTest {
    private final PassClient client = new JsonApiPassClient("http://localhost:8080/data");

    @Test
    public void testCreateSimpleObject() throws IOException {
        Publication pub = new Publication();
        pub.setIssue("issue");
        pub.setPmid("pmid");

        client.createObject(pub);

        assertNotNull(pub.getId());

        Publication test = client.getObject(Publication.class, pub.getId());

        assertEquals(pub, test);
    }

    @Test
    public void testCreateObjectWithRelationships() throws IOException {
        User pi = new User();
        pi.setDisplayName("Bessie Cow");
        pi.setRoles(Arrays.asList(UserRole.ADMIN));

        client.createObject(pi);

        Funder funder = new Funder();
        funder.setName("Farmer Bob");

        client.createObject(funder);

        Grant grant = new Grant();

        grant.setAwardNumber("award");
        grant.setLocalKey("localkey");
        grant.setAwardDate(ZonedDateTime.parse("2014-03-28T00:00:00.000Z", ZonedDateTimeAdapter.FORMATTER));
        grant.setDirectFunder(funder);
        grant.setPi(pi);

        client.createObject(grant);

        Grant test = client.getObject(Grant.class, grant.getId(), "directFunder", "pi");

        // grant.setDirectFunder(new Funder(funder.getId()));
        // grant.setPi(new User(pi.getId()));

        assertEquals(grant.getId(), test.getId());
        assertEquals(grant.getAwardNumber(), test.getAwardNumber());

        // System.err.println(grant.getAwardDate());
        // System.err.println(test.getAwardDate());

        // TODO Switch equals method to doing instant comparison

        assertEquals(grant.getAwardDate().toInstant(), test.getAwardDate().toInstant());

        assertEquals(grant.getDirectFunder().getId(), test.getDirectFunder().getId());
        assertEquals(grant.getDirectFunder().getLocalKey(), test.getDirectFunder().getLocalKey());
        assertEquals(grant.getDirectFunder().getUrl(), test.getDirectFunder().getUrl());

        assertEquals(grant.getDirectFunder().getName(), test.getDirectFunder().getName());
        assertEquals(grant.getDirectFunder().getPolicy(), test.getDirectFunder().getPolicy());

        assertEquals(grant.getDirectFunder(), test.getDirectFunder());

        assertEquals(grant.getPi(), test.getPi());

        assertEquals(grant, test);
    }

    @Test
    public void testSelectObjects() throws IOException {
        String pmid = "" + UUID.randomUUID();

        for (int i = 0; i < 10; i++) {
            Publication pub = new Publication();

            pub.setIssue("Number: " + i);
            pub.setTitle("Title: " + i);
            pub.setPmid(pmid);

            client.createObject(pub);
        }

        String filter = RSQL.equals("pmid", pmid);
        PassClientSelector<Publication> selector = new PassClientSelector<>(Publication.class, 0, 100, filter, "id");
        PassClientResult<Publication> result = client.selectObjects(selector);

        assertEquals(10, result.getTotal());
        assertEquals(10, result.getObjects().size());

        result.getObjects().forEach(p -> {
            assertNotNull(p.getId());
            assertTrue(p.getTitle().startsWith("Title"));
        });
    }
}
