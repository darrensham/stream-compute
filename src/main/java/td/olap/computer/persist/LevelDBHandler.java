package td.olap.computer.persist;

import java.io.File;
import java.io.IOException;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelDBHandler extends DBHandler {

	public static Options options = new Options();

	private static Logger logger = LoggerFactory.getLogger(LevelDBHandler.class);

	static {
		options.createIfMissing(true);
		options.cacheSize(100 * 1048576);
	}

	private String dbName;
	private DB db;

	public LevelDBHandler(String dbName) {
		this.dbName = dbName;

	}
	
	@Override
	public void open() {
		try {
			db = JniDBFactory.factory.open(new File(dbName), options);
		} catch (IOException e) {
			logger.error("Error when open db named " + dbName, e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		if (db != null)
			try {
				db.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		db = null;
	}

	public byte[] getByteWiseValue(String key) throws Exception {
		try {
			byte[] b = db.get(JniDBFactory.bytes(key));
			return b;
		} catch (Exception e) {
			open();
			throw new RuntimeException("Error when get value of " + key + " from db " + this.dbName + ".", e);
		}
	}

	@Override
	public String getStringValue(String key) throws Exception {
		return JniDBFactory.asString(getByteWiseValue(key));
	}
	
	@Override
	public void setKey(String key, byte[] value) {
		synchronized (options) {
			try {
				db.put(JniDBFactory.bytes(key), value);
			} catch (Exception e) {
				open();
				throw new RuntimeException(
					"Error when set value of " + key + " into db " + this.dbName + ".", e);
			}
		}
	}

	@Override
	public void setKey(String key, String value) {
		setKey(key, JniDBFactory.bytes(value));
	}

	@Override
	public void delKey(String key) {
		synchronized (options) {
			try {
				db.put(JniDBFactory.bytes(key), null);
			} catch (Exception e) {
				open();
				throw new RuntimeException(
					"Error when set value of " + key + " into db " + this.dbName + ".", e);
			}
		}
	}
}
