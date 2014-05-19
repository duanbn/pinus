package com.pinus.cluster.beans;

/**
 * 数据库表的列的bean. 表示一个数据库表的一个列.
 * 
 * @author duanbn
 */
public class DBTableColumn {

	/**
	 * 字段名
	 */
	private String field;

	/**
	 * 字段类型
	 */
	private String type;

	/**
	 * 字段长度
	 */
	private int length;

	/**
	 * 是否可以为空
	 */
	private boolean isCanNull;

	/**
	 * 是否是主键列
	 */
	private boolean isPrimaryKey;

	/**
	 * 是否是自增字段
	 */
	private boolean isAutoIncrement;

	/**
	 * 是否有默认值
	 */
	private boolean hasDefault;

	/**
	 * 默认值
	 */
	private Object defaultValue;

	/**
	 * 注释
	 */
	private String comment;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isCanNull() {
		return isCanNull;
	}

	public void setCanNull(boolean isCanNull) {
		this.isCanNull = isCanNull;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public void setPrimaryKey(boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public boolean isAutoIncrement() {
		return isAutoIncrement;
	}

	public void setAutoIncrement(boolean isAutoIncrement) {
		this.isAutoIncrement = isAutoIncrement;
	}

	public boolean isHasDefault() {
		return hasDefault;
	}

	public void setHasDefault(boolean hasDefault) {
		this.hasDefault = hasDefault;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + (hasDefault ? 1231 : 1237);
		result = prime * result + (isAutoIncrement ? 1231 : 1237);
		result = prime * result + (isCanNull ? 1231 : 1237);
		result = prime * result + (isPrimaryKey ? 1231 : 1237);
		result = prime * result + length;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DBTableColumn other = (DBTableColumn) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (hasDefault != other.hasDefault)
			return false;
		if (isAutoIncrement != other.isAutoIncrement)
			return false;
		if (isCanNull != other.isCanNull)
			return false;
		if (isPrimaryKey != other.isPrimaryKey)
			return false;
		if (length != other.length)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
