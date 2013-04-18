package wisematches.playground.propose.impl.file;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import wisematches.core.PersonalityManager;
import wisematches.core.Player;
import wisematches.core.personality.DefaultMember;
import wisematches.playground.GameSettings;
import wisematches.playground.MockGameSettings;
import wisematches.playground.propose.GameProposal;
import wisematches.playground.propose.ProposalRelation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class FileProposalManagerTest {
	private File file;
	private FileProposalManager<GameSettings> fileProposalManager;

	private static final Player P1 = new DefaultMember(901, null, null, null, null, null);
	private static final Player P2 = new DefaultMember(902, null, null, null, null, null);
	private static final Player P3 = new DefaultMember(903, null, null, null, null, null);

	public FileProposalManagerTest() {
	}

	@Before
	public void setUp() throws IOException {
		file = File.createTempFile("proposal-", "-unit-test");
		if (file.exists()) {
			file.delete();
		}

		fileProposalManager = new FileProposalManager<>();
		fileProposalManager.setProposalsResource(file);
		fileProposalManager.setTransactionTemplate(new TransactionTemplate() {
			@Override
			public <T> T execute(TransactionCallback<T> action) throws TransactionException {
				return action.doInTransaction(null);
			}
		});
	}

	@After
	public void cleanUp() {
		if (file.exists()) {
			file.delete();
		}
	}

	@Test
	public void storeGameProposal() throws Exception {
		System.out.println(file.getAbsolutePath());
		final GameSettings settings = new MockGameSettings("Mock", 3);

		long currentSize = file.length();
		fileProposalManager.initiate(settings, P1, 3);
		assertTrue(currentSize < (currentSize = file.length()));

		fileProposalManager.initiate(settings, "mock", P1, Arrays.asList(P2, P3));
		assertTrue(currentSize < (currentSize = file.length()));
	}

	@Test
	public void loadGameProposals() throws Exception {
		System.out.println(file.getAbsolutePath());
		final GameSettings settings = new MockGameSettings("Mock", 3);

		fileProposalManager.initiate(settings, P1, 3);
		fileProposalManager.initiate(settings, "mock", P1, Arrays.asList(P2, P3));
		fileProposalManager.close();

		fileProposalManager = new FileProposalManager<>();
		fileProposalManager.setTransactionTemplate(new TransactionTemplate() {
			@Override
			public <T> T execute(TransactionCallback<T> action) throws TransactionException {
				return action.doInTransaction(null);
			}
		});
		final PersonalityManager personalityManager = createNiceMock(PersonalityManager.class);
		expect(personalityManager.getPerson(P1.getId())).andReturn(P1);
		expect(personalityManager.getPerson(P2.getId())).andReturn(P2);
		expect(personalityManager.getPerson(P3.getId())).andReturn(P3);
		replay(personalityManager);

		fileProposalManager.setPersonalityManager(personalityManager);
		fileProposalManager.setProposalsResource(file);
		fileProposalManager.afterPropertiesSet();
		assertEquals(2, fileProposalManager.searchEntities(null, ProposalRelation.AVAILABLE, null, null).size());

		Object[] objects = fileProposalManager.searchEntities(null, ProposalRelation.AVAILABLE, null, null).toArray();
		@SuppressWarnings("unchecked")
		GameProposal<GameSettings> pl1 = (GameProposal<GameSettings>) objects[0];
		@SuppressWarnings("unchecked")
		GameProposal<GameSettings> pl2 = (GameProposal<GameSettings>) objects[0];

		assertTrue(pl1.getId() != 0);
		assertTrue(pl2.getId() != 0);

		final GameProposal<GameSettings> p3 = fileProposalManager.initiate(settings, P1, 3);
		assertTrue(p3.getId() > pl1.getId());
		assertTrue(p3.getId() > pl2.getId());
	}
}
