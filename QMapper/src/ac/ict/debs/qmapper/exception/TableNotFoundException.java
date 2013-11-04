package ac.ict.debs.qmapper.exception;

public class TableNotFoundException extends Exception {
	public TableNotFoundException(String excp) {
		super(excp);
	}

	public TableNotFoundException(String excp, Throwable e) {
		super(excp);
		this.initCause(e);
	}
}
