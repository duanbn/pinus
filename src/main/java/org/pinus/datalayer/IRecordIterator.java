package org.pinus.datalayer;

import java.util.Iterator;
import java.util.List;

import org.pinus.api.query.IQuery;

/**
 * 记录遍历器.
 * 
 * @author duanbn
 *
 * @param <E>
 */
public interface IRecordIterator<E> extends Iterator<E> {

	/**
	 * 批量返回
	 * 
	 * @return
	 */
	public List<E> nextMore();

	/**
	 * 获取此遍历器需要遍历的结果集总数.
	 * 
	 * @return
	 */
	public long getCount();

	/**
	 * 设置遍历时查询的条件
	 * 
	 * @param query
	 */
	public void setQuery(IQuery query);

}
