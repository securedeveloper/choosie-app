package com.choozie.app.controllers;

public class FeedCacheKey {
	private String cursor;
	private boolean append;
	private String fbUid;

	public FeedCacheKey(String cursor, boolean append, String fbUid) {
		this.setCursor(cursor);
		this.append = append;
		this.fbUid = fbUid;
	}

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

	public String getCursor() {
		return cursor;
	}

	public String getFbUid() {
		return fbUid;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FeedCacheKey)) {
			return false;
		}

		boolean isCursorsEqual = false;
		boolean isFbUidEqual = false;
		FeedCacheKey other = (FeedCacheKey) o;
		if (other.append != this.append) {
			return false;
		}
		if (this.getCursor() == null && other.getCursor() == null) {
			isCursorsEqual = true;
		}
		if (this.getCursor() != null && other.getCursor() == null) {
			return false;
		}
		if (this.getCursor() == null && other.getCursor() != null) {
			return false;
		}

		if ((this.getFbUid() == null) && (other.getFbUid() == null)) {
			isFbUidEqual = true;
		}

		if ((this.getFbUid() == null) && (other.getFbUid() != null)) {
			return false;
		}

		if ((this.getFbUid() != null) && (other.getFbUid() == null)) {
			return false;
		}

		if (isCursorsEqual == false) {
			if (this.getCursor().equals(other.getCursor())) {
				isCursorsEqual = true;
			}
		}

		if (isFbUidEqual == false) {
			if (this.getFbUid().equals(other.getFbUid())) {
				isFbUidEqual = true;
			}
		}

		return (isFbUidEqual && isCursorsEqual);

	}

	@Override
	public int hashCode() {
		if (this.getCursor() != null) {
			return (this.getCursor().hashCode() << 1) | (this.append ? 1 : 0);
		}
		return (this.append ? 1 : 0);
	}
}
