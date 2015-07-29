/**
 * Copyright 2014 Duan Bingnan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pinus4j.api.query;

import java.util.List;

import org.pinus4j.api.query.impl.Condition;
import org.pinus4j.api.query.impl.Order;
import org.pinus4j.cluster.enums.EnumDBMasterSlave;

/**
 * 查询对象.
 * 
 * @author duanbn
 */
public interface IQuery {

    /**
     * 获取一条记录. 如果有多条只获取第一条.
     * 
     * @return
     */
    public <T> T load();

    /**
     * 获取此Query查询到的结果集.
     * 
     * @return
     */
    public <T> List<T> list();

    /**
     * 获取此Query查询到的结果集数量.
     * 
     * @return
     */
    public Number count();

    /**
     * 设置查询主从库.
     * 
     * @param masterSlave
     * @return
     */
    public IQuery setMasterSlave(EnumDBMasterSlave masterSlave);

    /**
     * 设置查询是否使用缓存
     * 
     * @param useCache
     * @return
     */
    public IQuery setUseCache(boolean useCache);

    /**
     * 添加取值字段.
     * 
     * @param field 获取值的字段
     * @return
     */
    public IQuery setFields(String... field);

    /**
     * 添加取值字段.
     *
     * @param clazz class
     * @param field 获取值的字段
     * @return
     */
    public IQuery setFields(Class<?> clazz, String... field);

    /**
     * 添加查询条件.
     * 
     * @param cond 一个查询条件
     */
    public IQuery add(Condition cond);

    /**
     * 添加排序字段.
     * 
     * @param field 被排序字段
     * @param order 升序降序
     */
    public IQuery orderBy(String field, Order order, Class<?> clazz);

    /**
     * 分页参数.
     * 
     * @param start 开始偏移量
     * @param limit 页大小
     */
    public IQuery limit(int start, int limit);

    /**
     * 设置limit参数
     * 
     * @param limit limit
     */
    public IQuery limit(int limit);

}
