package wisematches.playground.scribble.settings.impl;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wisematches.personality.Personality;
import wisematches.playground.scribble.settings.BoardSettings;
import wisematches.playground.scribble.settings.BoardSettingsManager;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class HibernateBoardSettingsManager extends HibernateDaoSupport implements BoardSettingsManager {
	private boolean checkWordsDefault = true;
	private boolean clearMemoryDefault = true;
	private String tilesClassDefault = "tilesSetClassic";

	private final Lock lock = new ReentrantLock();
	private final Map<Personality, HibernateBoardSettings> cache = new WeakHashMap<Personality, HibernateBoardSettings>();

	public HibernateBoardSettingsManager() {
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public BoardSettings getScribbleSettings(Personality personality) {
		if (personality == null) {
			return new BoardSettings(clearMemoryDefault, checkWordsDefault, tilesClassDefault);
		}

		lock.lock();
		try {
			return getHibernateBoardSettings(personality).clone();
		} finally {
			lock.unlock();
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY, readOnly = false)
	public void setScribbleSettings(Personality personality, BoardSettings settings) {
		lock.lock();
		try {
			HibernateBoardSettings s = getHibernateBoardSettings(personality);
			s.update(settings);
			saveBoardSettings(s);
		} finally {
			lock.unlock();
		}
	}

	private HibernateBoardSettings getHibernateBoardSettings(Personality personality) {
		HibernateBoardSettings settings = cache.get(personality);
		if (settings == null) {
			settings = loadBoardSettings(personality);
			cache.put(personality, settings);
		}
		if (settings == null) {
			settings = new HibernateBoardSettings(personality.getId(), clearMemoryDefault, checkWordsDefault, tilesClassDefault);
			cache.put(personality, settings);
		}
		return settings;
	}

	private HibernateBoardSettings loadBoardSettings(Personality personality) {
		return getHibernateTemplate().get(HibernateBoardSettings.class, personality.getId());
	}

	private void saveBoardSettings(HibernateBoardSettings settings) {
		getHibernateTemplate().saveOrUpdate(settings);
	}

	public boolean isCheckWordsDefault() {
		return checkWordsDefault;
	}

	public void setCheckWordsDefault(boolean checkWordsDefault) {
		this.checkWordsDefault = checkWordsDefault;
	}

	public boolean isClearMemoryDefault() {
		return clearMemoryDefault;
	}

	public void setClearMemoryDefault(boolean clearMemoryDefault) {
		this.clearMemoryDefault = clearMemoryDefault;
	}

	public String getTilesClassDefault() {
		return tilesClassDefault;
	}

	public void setTilesClassDefault(String tilesClassDefault) {
		this.tilesClassDefault = tilesClassDefault;
	}
}
