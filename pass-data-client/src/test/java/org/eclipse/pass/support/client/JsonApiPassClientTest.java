package org.eclipse.pass.support.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.pass.support.client.model.AggregatedDepositStatus;
import org.eclipse.pass.support.client.model.Funder;
import org.eclipse.pass.support.client.model.Grant;
import org.eclipse.pass.support.client.model.Journal;
import org.eclipse.pass.support.client.model.Publication;
import org.eclipse.pass.support.client.model.Source;
import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.User;
import org.eclipse.pass.support.client.model.UserRole;
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

        Publication test = client.getObject(pub);

        assertEquals(pub, test);
    }

    @Test
    public void testCreateGetObject() throws IOException {
        User pi = new User();
        pi.setDisplayName("Bessie Cow");
        pi.setRoles(Arrays.asList(UserRole.ADMIN));

        client.createObject(pi);

        List<User> copis = new ArrayList<>();

        for (String name : Arrays.asList("Jessie Farmhand", "Cassie Farmhand")) {
            User user = new User();
            user.setDisplayName(name);
            user.setRoles(Arrays.asList(UserRole.SUBMITTER));

            client.createObject(user);
            copis.add(user);
        }

        Funder funder = new Funder();
        funder.setName("Farmer Bob");

        client.createObject(funder);

        Grant grant = new Grant();

        grant.setAwardNumber("award");
        grant.setLocalKey("localkey");
        grant.setAwardDate(ZonedDateTime.parse("2014-03-28T00:00:00.000Z", Util.dateTimeFormatter()));
        grant.setStartDate(ZonedDateTime.parse("2016-01-10T02:12:13.040Z", Util.dateTimeFormatter()));
        grant.setDirectFunder(funder);
        grant.setPi(pi);
        grant.setCoPis(copis);

        client.createObject(grant);

        // Get the grant with the relationship target objects included
        Grant test = client.getObject(grant, "directFunder", "pi", "coPis");

        assertEquals(grant, test);

        // Get the grant without the relationship target objects included
        test = client.getObject(grant);

        // Relationship targets should just have id
        grant.setDirectFunder(new Funder(funder.getId()));
        grant.setPi(new User(pi.getId()));
        grant.setCoPis(copis.stream().map(u -> new User(u.getId())).collect(Collectors.toList()));

        assertEquals(grant, test);

        // Get the grant with one relationship, other relationship targets should just have id
        test = client.getObject(grant, "directFunder");

        grant.setDirectFunder(funder);

        assertEquals(grant, test);
    }

    @Test
    public void testUpdateObject() throws IOException {

        Publication pub = new Publication();
        pub.setTitle("Ten puns");

        client.createObject(pub);

        Submission sub = new Submission();

        sub.setAggregatedDepositStatus(AggregatedDepositStatus.NOT_STARTED);
        sub.setSource(Source.PASS);
        sub.setPublication(pub);
        sub.setSubmitterName("Name");
        sub.setSubmitted(false);

        client.createObject(sub);

        assertEquals(sub, client.getObject(sub, "publication"));
    }

    @Test
    public void testSelectObjects() throws IOException {
        String pmid = "" + UUID.randomUUID();

        Journal journal = new Journal();
        journal.setJournalName("The ministry of silly walks");

        client.createObject(journal);

        List<Publication> pubs = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Publication pub = new Publication();

            pub.setIssue("Number: " + i);
            pub.setTitle("Title: " + i);
            pub.setPmid(pmid);
            pub.setJournal(journal);

            client.createObject(pub);
            pubs.add(pub);
        }

        String filter = RSQL.equals("pmid", pmid);
        PassClientSelector<Publication> selector = new PassClientSelector<>(Publication.class, 0, 100, filter, "id");
        selector.setInclude("journal");
        PassClientResult<Publication> result = client.selectObjects(selector);

        assertEquals(pubs.size(), result.getTotal());
        assertIterableEquals(pubs, result.getObjects());

        // Test selecting with an offset
        selector = new PassClientSelector<>(Publication.class, 5, 100, filter, "id");
        selector.setInclude("journal");
        result = client.selectObjects(selector);

        assertEquals(pubs.size(), result.getTotal());
        assertIterableEquals(pubs.subList(5, pubs.size()), result.getObjects());

        // Test using a stream which will make multiple calls. Do not include journal.
        selector = new PassClientSelector<>(Publication.class, 0, 2, filter, "id");
        pubs.forEach(p -> p.setJournal(new Journal(journal.getId())));
        assertIterableEquals(pubs, client.streamObjects(selector).collect(Collectors.toList()));
    }
}
