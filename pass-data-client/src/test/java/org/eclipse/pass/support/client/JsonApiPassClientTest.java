package org.eclipse.pass.support.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.pass.support.client.model.AggregatedDepositStatus;
import org.eclipse.pass.support.client.model.AwardStatus;
import org.eclipse.pass.support.client.model.Contributor;
import org.eclipse.pass.support.client.model.CopyStatus;
import org.eclipse.pass.support.client.model.Deposit;
import org.eclipse.pass.support.client.model.EventType;
import org.eclipse.pass.support.client.model.File;
import org.eclipse.pass.support.client.model.Funder;
import org.eclipse.pass.support.client.model.Grant;
import org.eclipse.pass.support.client.model.IntegrationType;
import org.eclipse.pass.support.client.model.Journal;
import org.eclipse.pass.support.client.model.PassEntity;
import org.eclipse.pass.support.client.model.PerformerRole;
import org.eclipse.pass.support.client.model.Policy;
import org.eclipse.pass.support.client.model.Publication;
import org.eclipse.pass.support.client.model.Publisher;
import org.eclipse.pass.support.client.model.Repository;
import org.eclipse.pass.support.client.model.RepositoryCopy;
import org.eclipse.pass.support.client.model.Source;
import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionEvent;
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
        grant.setAwardDate(dt("2014-03-28T00:00:00.000Z"));
        grant.setStartDate(dt("2016-01-10T02:12:13.040Z"));
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

    // TODO handle relationships...
    private void check_create_get(PassEntity obj) throws IOException {
        client.createObject(obj);

        assertNotNull(obj.getId());

        PassEntity test = client.getObject(obj);

        assertEquals(obj.getId(), test.getId());
    }

    private static ZonedDateTime dt(String s) {
        return ZonedDateTime.parse("2010-12-10T02:01:20.300Z", Util.dateTimeFormatter());
    }

    public void testAllObjects() {
        User pi = new User();
        pi.setAffiliation(Collections.singleton("affil"));
        pi.setDisplayName("Farmer Bob");
        pi.setEmail("farmerbob@example.com");
        pi.setFirstName("Bob");
        pi.setLastName("Bobberson");
        pi.setLocatorIds(Collections.singletonList("locator1"));
        pi.setMiddleName("Bobbit");
        pi.setOrcidId("23xx-xxxx-xxxx-xxxx");
        pi.setRoles(Arrays.asList(UserRole.SUBMITTER));
        pi.setUsername("farmerbob1");

        User copi = new User();
        copi.setAffiliation(Collections.singleton("barn"));
        copi.setDisplayName("Bessie The Cow");
        copi.setEmail("bessie@example.com");
        copi.setFirstName("Bessie");
        copi.setLastName("Cow");
        copi.setLocatorIds(Collections.singletonList("locator2"));
        copi.setMiddleName("The");
        copi.setOrcidId("12xx-xxxx-xxxx-xxxx");
        copi.setRoles(Arrays.asList(UserRole.SUBMITTER));
        copi.setUsername("bessie1");

        Repository repository = new Repository();

        repository.setAgreementText("I agree to everything.");
        repository.setDescription("Repository description");
        repository.setFormSchema("form schema");
        repository.setIntegrationType(IntegrationType.FULL);
        repository.setName("Barn repository");
        repository.setRepositoryKey("barn");
        repository.setSchemas(Arrays.asList(URI.create("http://example.com/schema")));
        repository.setUrl(URI.create("http://example.com/barn.html"));

        Policy policy = new Policy();

        policy.setDescription("This is a policy description");
        policy.setInstitution(URI.create("https://jhu.edu"));
        policy.setPolicyUrl(URI.create("http://example.com/policy/oa.html"));
        policy.setRepositories(Arrays.asList(repository));
        policy.setTitle("Policy title");


        Funder primary = new Funder();

        primary.setLocalKey("bovine");
        primary.setName("Bovines R Us");
        primary.setPolicy(policy);
        primary.setUrl(URI.create("http://example.com/bovine"));

        Funder direct = new Funder();

        direct.setLocalKey("icecream");
        direct.setName("Icecream is great");
        direct.setPolicy(policy);
        direct.setUrl(URI.create("http://example.com/ice"));

        Grant grant = new Grant();

        grant.setAwardDate(dt("2010-01-10T02:01:20.300Z"));
        grant.setAwardNumber("moo42");
        grant.setAwardStatus(AwardStatus.ACTIVE);
        grant.setCoPis(Arrays.asList(copi));
        grant.setDirectFunder(direct);
        grant.setPrimaryFunder(primary);
        grant.setEndDate(dt("2015-12-10T02:04:20.300Z"));
        grant.setLocalKey("moo:42");
        grant.setPi(pi);
        grant.setProjectName("Moo Thru revival");
        grant.setStartDate(dt("2011-02-13T01:05:20.300Z"));

        Submission submission = new Submission();
        submission.setAggregatedDepositStatus(AggregatedDepositStatus.ACCEPTED);
        submission.setEffectivePolicies(Arrays.asList(policy));
        submission.setGrants(Arrays.asList(grant));
        submission.setMetadata("metadata");
        submission.setPreparers(Arrays.asList(preparer));
        submission.setPublication(publication);
        submission.setSource(Source.PASS);
        submission.setSubmissionStatus(null);
        submission.setSubmitted(true);
        submission.setSubmittedDate(dt("2012-12-10T02:01:20.300Z"));
        submission.setSubmitter(pi);
        submission.setSubmitterEmail(URI.create("mailto:" + pi.getEmail()));
        submission.setSubmitterName(pi.getDisplayName());

        SubmissionEvent event = new SubmissionEvent();
        event.setComment("This is a comment.");
        event.setEventType(EventType.SUBMITTED);
        event.setLink(URI.create("http://example.com/link"));
        event.setPerformedBy(pi);
        event.setPerformedDate(dt("2010-12-10T02:01:20.300Z"));
        event.setPerformerRole(PerformerRole.SUBMITTER);
        event.setSubmission(submission);

        Publication publication = new Publication();

        publication.setDoi(null);
        publication.setIssue(null);
        publication.setJournal(null);
        publication.setPmid(null);
        publication.setPublicationAbstract(null);
        publication.setTitle(null);
        publication.setVolume(null);

        RepositoryCopy rc = new RepositoryCopy();
        rc.setAccessUrl(URI.create("http://example.com/repo/item"));
        rc.setCopyStatus(CopyStatus.ACCEPTED);
        rc.setExternalIds(Arrays.asList("rc1"));
        rc.setPublication(publication);
        rc.setRepository(repository);

        Publisher publisher = new Publisher();

        publisher.setName(null);
        publisher.setPmcParticipation(null);






        Journal journal = new Journal();

        journal.setIssns(null);
        journal.setJournalName(null);
        journal.setPmcParticipation(null);
        journal.setPublisher(publisher);




        File file = new File();

        file.setDescription(null);
        file.setFileRole(null);
        file.setMimeType(null);
        file.setName(null);
        file.setSubmission(null);
        file.setUri(null);


        Deposit deposit = new Deposit();

        deposit.setDepositStatus(null);
        deposit.setRepository(null);
        deposit.setRepositoryCopy(null);
        deposit.setDepositStatusRef(null);

        Contributor contrib = new Contributor();

        contrib.setAffiliation(null);
        contrib.setDisplayName(null);
        contrib.setEmail(null);
        contrib.setFirstName(null);
        contrib.setMiddleName(null);
        contrib.setLastName(null);
        contrib.setOrcidId(null);
        contrib.setPublication(null);
        contrib.setRoles(null);
        contrib.setUser(null);


    }
}
