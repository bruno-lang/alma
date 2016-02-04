package alma.lang;

public final class NoMatch extends RuntimeException {

	public final byte[] data;
	public final int iLock;
	public final int iError;
	public final ParseTree tree;

	public NoMatch(byte[] data, int iLock, int iError, ParseTree tree) {
		this.data = data;
		this.iLock = iLock;
		this.iError = iError;
		this.tree = tree;
	}

}
