package ma.glasnost.orika.test.community.issue26;

public abstract class AbstractEntity<ID extends AbstractOrderID> {
	private ID entityID;

	public AbstractEntity() { /* Required by Orika mapping */
		this(null);
	}

	public AbstractEntity(ID entityID) {
		this.entityID = entityID;
	}

	public ID getEntityID() {
		return entityID;
	}

	public void setEntityID(ID entityID) {
		this.entityID = entityID;
	}
}