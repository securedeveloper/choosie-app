package com.choozie.app.controllers;

public class FeedCacheKey {
	private String cursor;
	private boolean append;

	public FeedCacheKey(String cursor, boolean append) {
		this.setCursor(cursor);
		this.append = append;
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

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FeedCacheKey)) {
			return false;
		}
		FeedCacheKey other = (FeedCacheKey) o;
		if (other.append != this.append) {
			return false;
		}
		if (this.getCursor() == null && other.getCursor() == null) {
			return true;
		}
		if (this.getCursor() != null && other.getCursor() == null) {
			return false;
		}
		if (this.getCursor() == null && other.getCursor() != null) {
			return false;
		}
		return this.getCursor().equals(other.getCursor());
	}

	@Override
	public int hashCode() {
		if (this.getCursor() != null) {
			return (this.getCursor().hashCode() << 1) | (this.append ? 1 : 0);
		}
		return (this.append ? 1 : 0);
	}
}
